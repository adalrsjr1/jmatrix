package com.github.adalrsjr1.jmatrix

class DenseMatrix implements Matrix<Number> {

    private int rows = 0
    private int cols = 0
    
    private Number[][] values
    
    static Matrix of(int rows, int cols) {
        new DenseMatrix(rows, cols)
    }
    
    private DenseMatrix(int rows, int cols) {
        this.rows = rows
        this.cols = cols
        values = new Number[rows][cols]
        initValues()
    }
    
    private void initValues() {
        for(int i = 0; i < height(); i++) {
            Arrays.fill(values[i], 0)
        }
    }
    
    static Matrix of(List values) {
        new DenseMatrix(values as Number[][], 0, values.size(), values[0].size(), { v, s -> v + s})
    }
    
    static Matrix of(Object[][] values) {
        new DenseMatrix(values as Number[][], 0, values.length, values[0].length, { v, s -> v + s})  
    }
    
    private DenseMatrix(Number[][] newValues, Number scalar, int rows, int cols, Closure closure) {
        this.rows = rows
        this.cols = cols
        this.values = new Number[rows][cols]
        initValues(newValues, scalar, closure)
    }
    
    private void initValues(Number[][] values, Number scalar, Closure closure) {
        for(int i = 0; i < height(); i++) {
            for(int j = 0; j < width(); j++) {
                this.values[i][j] = closure(values[i][j], scalar)
            }
        }
    }
    
    static of(Matrix matrix) {
        new DenseMatrix(matrix)
    }
        
    private DenseMatrix(Matrix matrix) {
        this(matrix.height(), matrix.width())
    }
    
    @Override
    int width() {
        return cols
    }

    @Override
    int height() {
        return rows
    }
    
    def getAt(int i) {
        synchronized (this) {
            values[i]
        }
    }
    
    boolean equals(Matrix other) {
        for(int i = 0; i < height(); i++) {
            for(int j = 0; j < width(); j++) {
                if(this[i][j] != other[i][j]) {
                    return false
                }
            }
        }
        return true
    }

    @Override
    Matrix identity() {
        if(height() != width()) {
            throw new RuntimeException("Cannot create a identity matrix from a non squared matrix")    
        }
        
        def matrix = new DenseMatrix(height(), width())
        for(int i = 0; i < height(); i++) {
            for(int j = 0; j < width(); j++) {
                if(i == j) {
                    matrix[i][j] = 1
                }
            }
        }
        return matrix
    }

    @Override
    Matrix plus(Number scalar) {
        new DenseMatrix(values, scalar, height(), width(), { v, s -> v + s} )
    }

    @Override
    Matrix plus(Matrix other) {
        checkingForEqualDimensions(this, other)
        
        def newMatrix = DenseMatrix.of(height(), width())
        for(int i = 0; i < height(); i++) {
            for(int j = 0; j < width(); j++) {
                newMatrix[i][j] = this[i][j] + other[i][j]
            }
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
        new DenseMatrix(values, scalar, height(), width(),{ v, s -> v - s} )
    }

    @Override
    Matrix minus(Matrix other) {
        checkingForEqualDimensions(this, other)
        
        def newMatrix = DenseMatrix.of(height(), width())
        for(int i = 0; i < height(); i++) {
            for(int j = 0; j < width(); j++) {
                newMatrix[i][j] = this[i][j] - other[i][j]
            }
        }
        
        return newMatrix
    }

    @Override
    Matrix multiply(Number scalar) {
        new DenseMatrix(values, scalar, height(), width(),{ v, s -> v * s} )
    }

    @Override
    Matrix multiply(Matrix other) {
        checkingDimensionsForMultiplication(this, other)
        
        int n = height()
        int m = other.width()

        def newMatrix = DenseMatrix.of(n,m)

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
        new DenseMatrix(values, scalar, height(), width(),{ v, s -> v / s} )
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

        def newMatrix = DenseMatrix.of(height, width)
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
        def newMatrix = DenseMatrix.of(height, width * 2)

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
        Matrix newMatrix = DenseMatrix.of(width(), height())
        for(int i = 0; i < height(); i++) {
            for(int j = 0; j < width(); j++) {
                newMatrix[j][i] = this[i][j]
            }
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