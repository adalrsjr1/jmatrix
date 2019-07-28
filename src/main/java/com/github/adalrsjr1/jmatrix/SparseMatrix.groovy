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
        new SparseMatrix(rows, cols)
    }

    private SparseMatrix(int rows, int cols) {
        this.rows = rows
        this.cols = cols
        values = Collections.synchronizedMap([:])
    }

    static Matrix of(List values) {
        new SparseMatrix(values as Number[][], 0, values.size(), values[0].size(), { v, s -> v + s})
    }

    static Matrix of(Object[][] values) {
        new SparseMatrix(values as Number[][], 0, values.length, values[0].length, { v, s -> v + s})
    }

    private SparseMatrix(Number[][] newValues, Number scalar, int rows, int cols, Closure closure) {
        this.rows = rows
        this.cols = cols
        this.values = values = Collections.synchronizedMap([:])
        
        iteration() { v, i, j ->
            values[new Tuple(i,j)] = newValues[i][j]
        }
    }

    void iteration(int cores = 1, Closure closure) {
        int nCores = height() > 50 && width() > 50 ? N_CORES : cores
        def rangeRow = height() % nCores == 0 ? height() / nCores : height().intdiv(nCores) + 1

        CountDownLatch latch = new CountDownLatch(nCores)
        AtomicInteger t = new AtomicInteger(0)

        for(int k = 0; k < nCores; k++) {
            T_POOL.execute({
                int n = t.getAndIncrement()
                for(int i = n * rangeRow; i < (n+1) * rangeRow && i < height(); i++) {
                    for(int j = 0; j < width(); j++) {
                        closure(this, i, j)
                    }
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
        return 0
    }
    @Override
    public int height() {
        return 0
    }
    @Override
    public Matrix identity() {

        return null
    }
    @Override
    public Object getAt(int i) {
        def array = InnerArray.of(values, i)
        return array
    }
    
    private static class InnerArray {
        private final Map map
        private final int index

        private InnerArray(Map map, int index) {
            this.map = map
            this.index = index
        }

        static InnerArray of(Map map, int index) {
            new InnerArray(map, index)
        }

        def getAt(j) {
            map[new Tuple(index,j)]
        }
    }
    
    @Override
    public Matrix plus(Number scalar) {
        
        return null
    }
    @Override
    public Matrix plus(Matrix other) {
        
        return null
    }
    @Override
    public Matrix minus(Number scalar) {
        
        return null
    }
    @Override
    public Matrix minus(Matrix other) {
        
        return null
    }
    @Override
    public Matrix multiply(Number scalar) {
        
        return null
    }
    @Override
    public Matrix multiply(Matrix other) {
        
        return null
    }
    @Override
    public Matrix div(Number scalar) {
        
        return null
    }
    @Override
    public Matrix div(Matrix other) {
        
        return null
    }
    @Override
    public Matrix inverse() {
        
        return null
    }
    @Override
    public Matrix transpose() {
        
        return null
    }
    @Override
    public double determinant() {
        
        return 0
    }
    @Override
    public String prettyToString() {
        
        return null
    }
}
