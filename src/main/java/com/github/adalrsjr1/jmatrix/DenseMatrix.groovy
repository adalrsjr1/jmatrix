package com.github.adalrsjr1.jmatrix

import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import groovy.transform.CompileStatic

class DenseMatrix extends BaseMatrix {
    static Matrix of(int rows, int cols) {
        of(null, 0, rows, cols, {v, s -> v + s})
    }

    static Matrix of(List values) {
        of(values as Number[][], 0, values.size(), values[0].size(), { v, s -> v + s})
    }

    static Matrix of(Number[][] values) {
        of(values, 0, values.length, values[0].length, { v, s -> v + s})
    }

    static of(Matrix matrix) {
        of(matrix.values, 0, matrix.height(), matrix.width(), {v, s -> v+s})
    }
    
    static of(Number[][] newValues, Number scalar, int rows, int cols, Closure closure) {
        new DenseMatrix(newValues, scalar, rows, cols, closure)
    }
    
    private DenseMatrix(Number[][] newValues, Number scalar, int rows, int cols, Closure closure) {
        this.rows = rows
        this.cols = cols
        this.values = new Number[rows][cols]
        
        if(newValues == null) {
            initValues()
        }
        else {
            initValues(newValues, scalar, closure)
        }
        
    }
    
    private void initValues() {
        for(int i = 0; i < height(); i++) {
            Arrays.fill(values[i], 0)
        }
    }
    
    private void initValues(Number[][] values, Number scalar, Closure closure) {
        iteration() { matrix, i, j ->
            matrix[i][j] = closure(values[i][j], scalar)
        }
    }

    def getAt(int i) {
        synchronized (values[i]) {
            values[i]
        }
    }
}
