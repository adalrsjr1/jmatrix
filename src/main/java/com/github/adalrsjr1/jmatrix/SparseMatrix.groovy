package com.github.adalrsjr1.jmatrix

import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class SparseMatrix implements Matrix<Number> {
    private static int N_CORES = Runtime.getRuntime().availableProcessors()
    private static final ExecutorService T_POOL = Executors.newFixedThreadPool(2*N_CORES)

    private int rows = 0
    private int cols = 0

    private Map<Tuple, Number> values

    static Matrix of(int rows, int cols) {
        new SparseMatrix([:], 0, rows, cols, { v, s -> v + s })
    }

    static Matrix of(List values) {
        new SparseMatrix(matrixListToMap(values), 0, values.size(), values[0].size(), { v, s -> v + s})
    }

    static private Map<Tuple, Number> matrixListToMap(def values) {
        def map = [:]
        for(int i = 0; i < values.size(); i++) {
            for(int j = 0; j < values[0].size(); j++) {
                if(0 != values[i][j]) {
                    map[new Tuple(i, j)] = values[i][j]
                }
            }
        }

        return map
    }

    static Matrix of(Number[][] values) {
        new SparseMatrix(matrixListToMap(values), 0, values.size(), values[0].size(), { v, s -> v + s})
    }

    static Matrix of(Map<Tuple, Number> newValues, Number scalar, int rows, int cols, Closure closure) {
        new SparseMatrix(newValues, scalar, rows, cols, closure)
    }

    private SparseMatrix(Map<Tuple, Number> newValues, Number scalar, int rows, int cols, Closure closure) {
        this.rows = rows
        this.cols = cols
        values = newValues.isEmpty() ? [:] : newValues

        iteration() { matrix, i, j ->
            matrix[i][j] = newValues[new Tuple(i,j)]
            
        }

        this.values = Collections.synchronizedMap(values)

    }

    void iteration(int cores = 1, Closure closure) {
        int nCores = height() > 50 && width() > 50 ? N_CORES : cores
        def rangeRow = height() % nCores == 0 ? height() / nCores : height().intdiv(nCores) + 1

        CountDownLatch latch = new CountDownLatch(nCores)
        AtomicInteger t = new AtomicInteger(0)

        for(int k = 0; k < nCores; k++) {
            T_POOL.execute({
                int n = t.getAndIncrement()
                try {
                    for(int i = n * rangeRow; i < (n+1) * rangeRow && i < height(); i++) {
                        for(int j = 0; j < width(); j++) {
                            closure(this, i, j)
                        }
                    }
                }
                catch(Exception e) {
                    Thread.currentThread().interrupt()
                    throw new RuntimeException(e)
                }
                latch.countDown()
            })
        }
        latch.await()
    }

    static of(Matrix matrix) {
        new SparseMatrix(matrix)
    }

    private SparseMatrix(Matrix matrix) {
        this(matrix.height(), matrix.width())
    }

    @Override
    public int width() {
        return this.cols
    }
    @Override
    public int height() {
        return this.rows
    }

    @Override
    public Matrix identity() {
        if(height() != width()) {
            throw new RuntimeException("Cannot create a identity matrix from a non squared matrix")
        }

        def matrix = SparseMatrix.of(height(), width())
        for(int i = 0; i < height(); i++) {
            matrix[i][i] = 1
        }
        return matrix
    }

    @Override
    public Object getAt(int i) {
        if(i >= height()) {
            throw new ArrayIndexOutOfBoundsException(i)
        }
        def array = InnerArray.of(values, i, width(), height())
        return array
    }

    public Object putAt(int i, Number value) {
        if(i >= height()) {
            throw new ArrayIndexOutOfBoundsException(i)
        }
        def array = InnerArray.of(values, i, width(), height())
        return array
    }

    private static class InnerArray {
        private final Map map
        private final int index

        private final int width
        private final int height

        private InnerArray(Map map, int index, int width, int height) {
            this.map = map
            this.index = index
            this.width = width
            this.height = height
        }

        static InnerArray of(Map map, int index, int width, int height) {
            new InnerArray(map, index, width, height)
        }

        def getAt(j) {
            def value = map.get(new Tuple(index, j), 0)
            if(j >= width) {
                throw new ArrayIndexOutOfBoundsException(j)
            }

            return value
        }

        def putAt(j, value) {
            if(value == 0 || value == null) {
                return
            }

            if(j >= width) {
                throw new ArrayIndexOutOfBoundsException(j)
            }

            def key = new Tuple(index, j)
            if(map.containsKey(key) && 0 == value) {
                map.remove(key)
                return map
            }

            map[key] = value
        }
    }

    boolean equals(Matrix other) {
        iteration() { matrix, i, j ->
            if(matrix[i][j] != other[i][j]) {
                return false
            }
        }
        return true
    }
    
    boolean equals(Matrix other, double epsilon) {
        iteration() { matrix, i, j ->
            return Math.abs(this[i][j]-other[i][j]) >= epsilon
        }
        return true
    }

    @Override
    Matrix plus(Number scalar) {
        SparseMatrix.of(values, scalar, height(), width(), { v, s -> v + s} )
    }

    @Override
    Matrix plus(Matrix other) {
        checkingForEqualDimensions(this, other)

        def newMatrix = SparseMatrix.of(height(), width())
        iteration() { matrix, i, j ->
            newMatrix[i][j] = matrix[i][j] + other[i][j]
        }

        return newMatrix
    }

    private void checkingForEqualDimensions(Matrix m1, Matrix m2) {
        if(m1.height() != m2.height() || m1.width() != m2.width()) {
            throw new RuntimeException("Both matrix should have same dimensions")
        }
    }

    @Override
    Matrix minus(Number scalar) {
        SparseMatrix.of(values, scalar, height(), width(),{ v, s -> v - s} )
    }

    @Override
    Matrix minus(Matrix other) {
        checkingForEqualDimensions(this, other)

        def newMatrix = SparseMatrix.of(height(), width())
        iteration() { matrix, i, j ->
            newMatrix[i][j] = matrix[i][j] - other[i][j]
        }

        return newMatrix
    }

    @Override
    Matrix multiply(Number scalar) {
        SparseMatrix.of(values, scalar, height(), width(),{ v, s -> v * s} )
    }

    @Override
    Matrix multiply(Matrix other) {
        checkingDimensionsForMultiplication(this, other)

        int n = height()
        int m = other.width()

        def newMatrix = SparseMatrix.of(n,m)

        for(int i = 0; i < height(); i++) {
            for(int j = 0; j < other.width(); j++) {
                for(int k = 0; k < width(); k++) {
                    newMatrix[i][j] += this[i][k] * other[k][j]
                }
            }
        }

        return newMatrix
    }

    private void checkingDimensionsForMultiplication(Matrix m1, Matrix m2) {
        if(!(m1.width() == m2.height())) {
            throw new RuntimeException("The M1's height should be equals to M2's width")
        }
    }

    @Override
    Matrix div(Number scalar) {
        SparseMatrix.of(values, scalar, height(), width(),{ v, s -> v / s} )
    }

    @Override
    Matrix div(Matrix other) {
        return new RuntimeException("Cannot divide one matrix to another")
    }

    @Override
    Matrix inverse() {
        Matrix expandedValues = gaussJordanExpandedMatrix(this)

        gaussJordanElimination(expandedValues)

        int height = expandedValues.height()
        int width = height

        def newMatrix = SparseMatrix.of(height, width)
        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                newMatrix[i][j] = expandedValues[i][j+width]
            }
        }

        return newMatrix
    }

    private Matrix gaussJordanExpandedMatrix(Matrix matrix) {
        int height = matrix.height()
        int width = matrix.width()
        def newMatrix = SparseMatrix.of(height, width * 2)

        // initialize
        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                newMatrix[i][j] = matrix[i][j]
            }
            newMatrix[i][i+width] = 1
        }

        return newMatrix
    }

    /**
     * http://www.matrixlab-examples.com/Modified-Gauss-Jordan-Algorithm.html
     * @param values
     */
    private void gaussJordanElimination(Matrix matrix) {
        int height = matrix.height()
        int width = matrix.width()

        int n = height

        def a = matrix

        def diagonal = diagonalOrdered(matrix, height)
        boolean[] marked = new boolean[height]

        for(int k = 0; k < height; k++) {

            Tuple2 pivotTuple = diagonal.poll()

            while(marked[pivotTuple.v1]) {
                pivotTuple = diagonal.poll()
            }

            int p = pivotTuple.v1
            double pp = pivotTuple.v2
            marked[p] = true


            for(int j = 0; j < width; j++) {
                a[p][j] = a[p][j] / pp
            }
            a[p][p] = 1 // to minimize the truncation error
            for(int i = 0; i < height; i++) {
                double pi = a[i][p]
                for(int j = 0; j < width; j++) {
                    if(i != p) {
                        a[i][j] = a[i][j] - a[p][j] * pi
                    }
                }

            }

            diagonal = diagonalOrdered(matrix, height)
        }
    }

    private PriorityQueue diagonalOrdered(Matrix matrix, int height) {
        Comparator comparator = new Comparator() {
                    public int compare(def v1, def v2) {
                        Double.compare(v2.second.abs(), v1.second.abs())
                    }
                }

        PriorityQueue orderedDiagonal = new PriorityQueue(height, comparator)

        for(int i = 0; i < height; i++) {
            orderedDiagonal.add(new Tuple2(i, matrix[i][i]))
        }

        return orderedDiagonal
    }

    @Override
    Matrix transpose() {
        Matrix newMatrix = SparseMatrix.of(width(), height())

        iteration() { matrix, i, j ->
            newMatrix[j][i] = matrix[i][j]
        }

        return newMatrix
    }

    @Override
    double determinant() {
        throw new RuntimeException("determinant not implemented yet")
    }

    String toString() {
        values.toString()
    }

    String prettyToString() {
        StringBuffer sb = new StringBuffer()

        sb.append("[\n")
        for(int i = 0; i < height(); i++) {
            def joiner = this[i].inject(new StringJoiner(", ")) { acc, value ->
                acc.add(value.toString())
            }
            sb.append("  [")
                    .append(joiner.toString())
                    .append("]\n")

        }
        sb.append("]")
        return sb.toString()
    }
}
