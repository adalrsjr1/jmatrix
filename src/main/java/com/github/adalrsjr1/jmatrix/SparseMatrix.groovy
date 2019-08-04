package com.github.adalrsjr1.jmatrix

import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

final class SparseMatrix extends BaseMatrix {
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

    static of(Matrix matrix) {
        of(matrix.values, 0, matrix.height(), matrix.width(), { s, v -> 0 })
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
}
