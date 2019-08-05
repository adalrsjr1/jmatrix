package com.github.adalrsjr1.testing.jmatrix

import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.RepeatedTest
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.RunnerException
import org.openjdk.jmh.runner.options.Options
import org.openjdk.jmh.runner.options.OptionsBuilder
import com.github.adalrsjr1.jmatrix.DenseMatrix
import com.github.adalrsjr1.jmatrix.Matrix
import com.github.adalrsjr1.jmatrix.SparseMatrix

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 2, jvmArgs = ["-Xms2G", "-Xmx2G"])
class MatrixBenchmark {
    // http://alblue.bandlem.com/Page/3/index.html
    @Param(["10", "100", "1000", "10000", "100000"])
    private int N

    static void main(String[] args) throws RunnerException {

        Options opt = new OptionsBuilder()
                .include(MatrixBenchmark.class.getSimpleName())
                .forks(1)
                .build()

        new Runner(opt).run();
    }

    @Benchmark
    void testParallelIterationLargeSquareDenseMatrix() {
        Matrix matrix = DenseMatrix.of(N, N)
        int scalar  = 0

        matrix.iteration(4) { v, i, j ->
            v[i][j] = scalar
        }
    }

    @Benchmark
    void testIterationLargeSquareDenseMatrix() {
        Matrix matrix = DenseMatrix.of(N, N)
        int scalar  = 0

        matrix.iteration() { v, i, j ->
            v[i][j] = scalar
        }
    }

    @Benchmark
    void testParallelIterationLargeSquareSparseMatrix() {
        Matrix matrix = SparseMatrix.of(N, N)
        int scalar  = 0

        matrix.iteration(4) { v, i, j ->
            v[i][j] = scalar
        }
    }

    @Benchmark
    void testIterationLargeSquareSparseMatrix() {
        Matrix matrix = SparseMatrix.of(N, N)
        int scalar  = 0

        matrix.iteration() { v, i, j ->
            v[i][j] = scalar
        }
    }
}
