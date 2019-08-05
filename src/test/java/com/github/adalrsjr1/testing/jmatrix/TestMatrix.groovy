package com.github.adalrsjr1.testing.jmatrix

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import com.github.adalrsjr1.jmatrix.BaseMatrix
import com.github.adalrsjr1.jmatrix.DenseMatrix
import com.github.adalrsjr1.jmatrix.Matrix
import com.github.adalrsjr1.jmatrix.SparseMatrix
import groovy.test.GroovyAssert

class TestMatrix extends GroovyAssert {

    static boolean assertMatrixEquals(Matrix expected, Matrix actual, double delta=0.0) {
        int height = expected.height()
        int width = expected.width()

        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                if(Math.abs(actual[i][j] - expected[i][j]) > delta) {
                    String message = String.format("actual(%f) != expected(%f) at [%d][%d]", actual[i][j].doubleValue(), expected[i][j].doubleValue(), i, j)
                    org.hamcrest.MatcherAssert.assertThat(message, true)
                    return false
                }
            }
        }
        return true
    }

    private static boolean assertionNot(String message, double actualValue, double expectedValue, double epsilon) {
        boolean result = !error(actualValue, expectedValue, epsilon)
        org.hamcrest.MatcherAssert.assertThat(message, result)

        return result
    }

    static boolean assertDouble(double actual, double expected, double epsilon, String message) {
        def result = error(actual, expected, epsilon)
        org.hamcrest.MatcherAssert.assertThat(message, result)
        return result
    }

    @ParameterizedTest
    @ValueSource(classes = [DenseMatrix, SparseMatrix])
    void creationOfMatrix_0x0(Class matrixClass) {
        def matrix = matrixClass.of(0,0)
        assert 0 == matrix.width()
        assert 0 == matrix.height()
    }

    @ParameterizedTest
    @ValueSource(classes = [DenseMatrix, SparseMatrix])
    void creationOfMatrix_1x1_notNull(Class matrixClass) {
        def matrix = matrixClass.of(1,1)
        assert 0 == matrix[0][0]
    }

    @ParameterizedTest
    @ValueSource(classes = [DenseMatrix, SparseMatrix])
    void creationOfSquareMatrix_2x2(Class matrixClass) {
        def matrix = matrixClass.of(2,2)
        assert 2 == matrix.width()
        assert 2 == matrix.height()
    }

    @ParameterizedTest
    @ValueSource(classes = [DenseMatrix, SparseMatrix])
    void creationOfNonSquareMatrix_3x2(Class matrixClass) {
        def matrix = matrixClass.of(3,2)
        assert 3 == matrix.height()
        assert 2 == matrix.width()
    }

    @ParameterizedTest
    @ValueSource(classes = [DenseMatrix, SparseMatrix])
    void getValidPosition(Class matrixClass) {
        def matrix = matrixClass.of(3,2)
        matrix[2][1]
    }

    @ParameterizedTest
    @ValueSource(classes = [DenseMatrix, SparseMatrix])
    void getInvalidPosition(Class matrixClass) {
        def matrix = matrixClass.of(3,2)
        shouldFail(ArrayIndexOutOfBoundsException) {
            matrix[3][2]
        }
    }

    @ParameterizedTest
    @ValueSource(classes = [DenseMatrix, SparseMatrix])
    void givenValidPositionSetValue(Class matrixClass) {
        def matrix = matrixClass.of(3,2)
        matrix[2][1] = 1
        assert 1 == matrix[2][1]
    }

    @ParameterizedTest
    @ValueSource(classes = [DenseMatrix, SparseMatrix])
    void givenInvalidPositionSetValue(Class matrixClass) {
        def matrix = matrixClass.of(3,3)
        shouldFail(ArrayIndexOutOfBoundsException) {
            matrix[3][3] = 1
        }
    }
    
    @ParameterizedTest
    @ValueSource(classes = [DenseMatrix, SparseMatrix])
    void givenInvalidPositionSetValue32(Class matrixClass) {
        def matrix = matrixClass.of(3,3)
        shouldFail(ArrayIndexOutOfBoundsException) {
            matrix[3][2] = 1
        }
    }
    
    @ParameterizedTest
    @ValueSource(classes = [DenseMatrix, SparseMatrix])
    void givenInvalidPositionSetValue23(Class matrixClass) {
        def matrix = matrixClass.of(3,3)
        shouldFail(ArrayIndexOutOfBoundsException) {
            matrix[2][3] = 1
        }
    }

    @ParameterizedTest
    @ValueSource(classes = [DenseMatrix, SparseMatrix])
    void validIdentity(Class matrixClass) {
        def matrix = matrixClass.of(3,3)
        assert matrixClass.of([[1,0, 0], [0,1,0], [0,0,1]]) == matrix.identity()
    }

    @ParameterizedTest
    @ValueSource(classes = [DenseMatrix, SparseMatrix])
    void invalidIdentity(Class matrixClass) {
        def matrix = matrixClass.of(2,3)

        shouldFail(RuntimeException) {
            matrix.identity()
        }
    }

    @ParameterizedTest
    @ValueSource(classes = [DenseMatrix, SparseMatrix])
    void sumToScalar2(Class matrixClass) {
        def matrix = matrixClass.of(2, 2)

        assert matrixClass.of([[2,2],[2,2]]) == matrix + 2
    }

    @ParameterizedTest
    @ValueSource(classes = [DenseMatrix, SparseMatrix])
    void sumToAnotherMatrix(Class matrixClass) {
        assert matrixClass.of([[0,2],[4,6]]) == matrixClass.of([[0,1],[2,3]]) + matrixClass.of([[0,1],[2,3]])

    }

    @ParameterizedTest
    @ValueSource(classes = [DenseMatrix, SparseMatrix])
    void subToScalar2(Class matrixClass) {
        def matrix = matrixClass.of(2, 2)

        assert matrixClass.of([[-2,-2],[-2,-2]]) == matrix - 2
    }

    @ParameterizedTest
    @ValueSource(classes = [DenseMatrix, SparseMatrix])
    void subFromAnotherMatrix(Class matrixClass) {
        assert matrixClass.of([[0,0],[0,0]]) == matrixClass.of([[0,1],[2,3]]) - matrixClass.of([[0,1],[2,3]])
    }

    @ParameterizedTest
    @ValueSource(classes = [DenseMatrix, SparseMatrix])
    void multToScalar3(Class matrixClass) {
        assert matrixClass.of([[0,3],[6,9]]) == matrixClass.of([[0,1],[2,3]]) * 3
    }

    @ParameterizedTest
    @ValueSource(classes = [DenseMatrix, SparseMatrix])
    void testMultSameDimension(Class matrixClass) {
        def m1 = matrixClass.of([
            [1, 2, 3],
            [4, 5, 6],
            [7, 8, 9]])

        def m2 = matrixClass.of([
            [9, 8, 7],
            [6, 5, 4],
            [3, 2, 1]])

        def m3 = m1 * m2

        def result = matrixClass.of([
            [30, 24, 18],
            [84, 69, 54],
            [138, 114, 90]])

        assert result == m3
    }

    @ParameterizedTest
    @ValueSource(classes = [DenseMatrix, SparseMatrix])
    void testMultInvalidDimension(Class matrixClass) {
        def m1 = matrixClass.of([
            [1, 2, 3],
            [4, 5, 6],
        ])

        def m2 = matrixClass.of([
            [9, 8, 7],
            [6, 5, 4],
        ])

        shouldFail() { def m3 = m1 * m2 }
    }

    @ParameterizedTest
    @ValueSource(classes = [DenseMatrix, SparseMatrix])
    void testMultDifferentDimensionsShouldPass(Class matrixClass) {
        def m1 = matrixClass.of([
            [1, 2, 3],
            [4, 5, 6],
        ])

        def m2 = matrixClass.of([
            [9, 8],
            [6, 5],
            [3, 2] ])

        def m3 = m1 * m2

        def result = matrixClass.of([
            [30, 24],
            [84, 69],
        ])

        assert result == m3
    }

    @ParameterizedTest
    @ValueSource(classes = [DenseMatrix, SparseMatrix])
    void testMultDifferentDimensions13x31ShouldPass(Class matrixClass) {
        def m1 = matrixClass.of([[4, 5, 6],])

        def m2 = matrixClass.of([[7], [8], [9]])

        def m3 = m1 * m2

        def result = matrixClass.of([[122],])

        assert result == m3
    }

    @ParameterizedTest
    @ValueSource(classes = [DenseMatrix, SparseMatrix])
    void testMultDifferentDimensions23x31ShouldPass(Class matrixClass) {
        def m1 = matrixClass.of([
            [7, 8, 9],
            [4, 5, 6],
        ])

        def m2 = matrixClass.of([[7], [8], [9]])

        def m3 = m1 * m2

        def result = matrixClass.of([[194], [122],])

        assert result == m3
    }

    @ParameterizedTest
    @ValueSource(classes = [DenseMatrix, SparseMatrix])
    void divToScalar2(Class matrixClass) {
        assert matrixClass.of([[2,2],[2,2]]) == matrixClass.of([[6,6],[6,6]]) / 3
    }

    @ParameterizedTest
    @ValueSource(classes = [DenseMatrix, SparseMatrix])
    void transposing(Class matrixClass) {
        assert matrixClass.of([[1,2],[3,4],[5,6]]) == matrixClass.of([[1,3,5], [2,4,6]]).transpose()
    }

    @ParameterizedTest
    @ValueSource(classes = [DenseMatrix, SparseMatrix])
    void testGaussJordanExpansion(Class matrixClass) {
        def m = matrixClass.of(0,0)

        def result = (m as BaseMatrix).gaussJordanExpandedMatrix(matrixClass.of([
            [1, 2, 3],
            [4, 5, 6],
            [7, 8, 9]]))

        assert matrixClass.of([
            [1, 2, 3, 1, 0, 0],
            [4, 5, 6, 0, 1, 0],
            [7, 8, 9, 0, 0, 1]]) == result
    }

    @ParameterizedTest
    @ValueSource(classes = [DenseMatrix, SparseMatrix])
    void testGetLargestAbsoluteDiagonalElement(Class matrixClass) {
        def m = matrixClass.of(0,0)

        def values = matrixClass.of([
            [1, 2, 3, 1, 0, 0],
            [4, 5, 6, 0, 1, 0],
            [7, 8, 9, 0, 0, 1]])

        def q = m.diagonalOrdered(values, 3)

        assertEquals(new Tuple2(2, 9.0), q.poll())
        assertEquals(new Tuple2(1, 5.0), q.poll())
        assertEquals(new Tuple2(0, 1.0), q.poll())
    }

    @ParameterizedTest
    @ValueSource(classes = [DenseMatrix, SparseMatrix])
    void testGivenNegativeValueGetLargestAbsoluteDiagonalElement(Class matrixClass) {
        def m = matrixClass.of(0,0)

        def values = matrixClass.of([
            [1, 2, 3, 1, 0, 0],
            [4, -10, 6, 0, 1, 0],
            [7, 8, 9, 0, 0, 1]] )

        def q = m.diagonalOrdered(values, 3)

        assertEquals(new Tuple2(1, -10.0), q.poll())
        assertEquals(new Tuple2(2, 9.0), q.poll())
        assertEquals(new Tuple2(0, 1.0), q.poll())
    }

    @ParameterizedTest
    @ValueSource(classes = [DenseMatrix, SparseMatrix])
    void testInverse1(Class matrixClass) {
        Matrix m = matrixClass.of([
            [-1, -1, 3],
            [2, 1, 2],
            [-2, -2, 1]
        ])

        m = m.inverse()

        assertMatrixEquals(matrixClass.of([[-1,1,1], 
            [1.2, -1, -1.6], 
            [0.4, 0, -0.2]]), m, 0.1)
    }

    @ParameterizedTest
    @ValueSource(classes = [DenseMatrix, SparseMatrix])
    void testInverse2(Class matrixClass) {
        Matrix m = matrixClass.of([
            [3, 3.5],
            [3.2, 3.6],
        ])

        m = m.inverse()

        assertMatrixEquals(matrixClass.of([[-9, 8.75],
        [8, -7.5]]), m, 0.1)
    }

    @ParameterizedTest
    @ValueSource(classes = [DenseMatrix, SparseMatrix])
    void testInverse3(Class matrixClass) {
        Matrix m = matrixClass.of([
            [1, 2, 3],
            [0, 1, 4],
            [5, 6, 0]
        ])

        m = m.inverse()
        assertMatrixEquals(matrixClass.of([
        [-24, 18, 5],
        [20, -15, -4], 
        [-5, 4, 1]]), m, 0.1)
    }
}
