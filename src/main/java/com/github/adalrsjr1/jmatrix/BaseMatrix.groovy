package com.github.adalrsjr1.jmatrix

import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

abstract class BaseMatrix<T> implements Matrix<Number> {
    protected static int N_CORES = Runtime.getRuntime().availableProcessors()
    protected static final ExecutorService T_POOL = Executors.newFixedThreadPool(2*N_CORES)

    protected int rows = 0
    protected int cols = 0
    
    protected volatile T values
    
    T getValues() {
        values
    }
    
    static Matrix of(Class clazz, int rows, int cols) {
        if(SparseMatrix == clazz) {
            return SparseMatrix.of(rows, cols)
        }
        
        if(DenseMatrix == clazz) {
            return DenseMatrix.of(rows, cols)
        }
    }

    static Matrix of(Class clazz, List values) {
        if(SparseMatrix == clazz) {
            return SparseMatrix.of(this.class, values)
        }
        
        if(DenseMatrix == clazz) {
            return DenseMatrix.of(this.class, values)
        }
        
    }

    static Matrix of(Class clazz, Number[][] values) {
        if(SparseMatrix == clazz) {
            return SparseMatrix.of(this.class, values)
        }
        
        if(DenseMatrix == clazz) {
            return DenseMatrix.of(this.class, values)
        }
    }

    static Matrix of(Class clazz, def newValues, Number scalar, int rows, int cols, Closure closure) {
        if(SparseMatrix == clazz) {
            return SparseMatrix.of(newValues, scalar, rows, cols, closure)
        }
        
        if(DenseMatrix == clazz) {
            return DenseMatrix.of(newValues, scalar, rows, cols, closure)
        }
    }
    
    static Matrix of(Matrix matrix) {
        if(matrix instanceof SparseMatrix) {
            return SparseMatrix.of(matrix)
        }
        
        if(matrix instanceof DenseMatrix) {
            return DenseMatrix.of(matrix)
        }
    }
    
    @Override
    public int width() {
        return this.cols
    }
    @Override
    public int height() {
        return this.rows
    }
    
    boolean equals(Matrix other) {
        iteration() { matrix, i, j ->
            if(matrix[i][j] != other[i][j]) {
                return false
            }
        }
        return true
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
    
    boolean equals(Matrix other, double epsilon) {
        iteration() { matrix, i, j ->
            return Math.abs(this[i][j]-other[i][j]) >= epsilon
        }
        return true
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
    
    @Override
    public Matrix identity() {
        if(height() != width()) {
            throw new RuntimeException("Cannot create a identity matrix from a non squared matrix")
        }

        def matrix = BaseMatrix.of(this.class, height(), width())
        for(int i = 0; i < height(); i++) {
            matrix[i][i] = 1
        }
        return matrix
    }
    
    @Override
    Matrix plus(Number scalar) {
        BaseMatrix.of(this.class, values, scalar, height(), width(), { v, s -> v + s} )
    }

    @Override
    Matrix plus(Matrix other) {
        checkingForEqualDimensions(this, other)

        def newMatrix = BaseMatrix.of(this.class, height(), width())
        iteration() { matrix, i, j ->
            newMatrix[i][j] = matrix[i][j] + other[i][j]
        }

        return newMatrix
    }

    protected void checkingForEqualDimensions(Matrix m1, Matrix m2) {
        if(m1.height() != m2.height() || m1.width() != m2.width()) {
            throw new RuntimeException("Both matrix should have same dimensions")
        }
    }

    @Override
    Matrix minus(Number scalar) {
        BaseMatrix.of(this.class, values, scalar, height(), width(),{ v, s -> v - s} )
    }

    @Override
    Matrix minus(Matrix other) {
        checkingForEqualDimensions(this, other)

        def newMatrix = BaseMatrix.of(this.class, height(), width())
        iteration() { matrix, i, j ->
            newMatrix[i][j] = matrix[i][j] - other[i][j]
        }

        return newMatrix
    }

    @Override
    Matrix multiply(Number scalar) {
        BaseMatrix.of(this.class, values, scalar, height(), width(),{ v, s -> v * s} )
    }

    @Override
    Matrix multiply(Matrix other) {
        checkingDimensionsForMultiplication(this, other)

        int n = height()
        int m = other.width()

        def newMatrix = BaseMatrix.of(this.class, n,m)

        for(int i = 0; i < height(); i++) {
            for(int j = 0; j < other.width(); j++) {
                for(int k = 0; k < width(); k++) {
                    newMatrix[i][j] += this[i][k] * other[k][j]
                }
            }
        }

        return newMatrix
    }

    protected void checkingDimensionsForMultiplication(Matrix m1, Matrix m2) {
        if(!(m1.width() == m2.height())) {
            throw new RuntimeException("The M1's height should be equals to M2's width")
        }
    }

    @Override
    Matrix div(Number scalar) {
        BaseMatrix.of(this.class, values, scalar, height(), width(),{ v, s -> v / s} )
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

        def newMatrix = BaseMatrix.of(this.class, height, width)
        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                newMatrix[i][j] = expandedValues[i][j+width]
            }
        }

        return newMatrix
    }

    protected Matrix gaussJordanExpandedMatrix(Matrix matrix) {
        int height = matrix.height()
        int width = matrix.width()
        def newMatrix = BaseMatrix.of(this.class, height, width * 2)

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
    protected void gaussJordanElimination(Matrix matrix) {
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

    protected PriorityQueue diagonalOrdered(Matrix matrix, int height) {
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
        Matrix newMatrix = BaseMatrix.of(this.class, width(), height())

        iteration() { matrix, i, j ->
            newMatrix[j][i] = matrix[i][j]
        }

        return newMatrix
    }
}
