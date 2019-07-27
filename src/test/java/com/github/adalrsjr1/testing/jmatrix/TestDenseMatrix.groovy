package com.github.adalrsjr1.testing.jmatrix

import com.github.adalrsjr1.jmatrix.DenseMatrix
import com.github.adalrsjr1.jmatrix.Matrix
import groovy.test.GroovyAssert
import org.junit.jupiter.api.Test

class TestDenseMatrix extends GroovyAssert {

    static boolean assertMatrixEquals(Matrix expected, Matrix actual, double delta=0.0) {

        assertMatrixEqualsInternals(expected, actual, delta, TestDenseMatrix.&assertion)
    }

    static private boolean assertMatrixEqualsInternals(Matrix expected, Matrix actual, double delta, Closure innerAssertion) {
        int height = expected.height()
        int width = expected.width()

        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                if(actual[i][j] != expected[i][j]) {
                    String str = String.format("actual(%f) != expected(%f) at [%d][%d]", actual[i][j].doubleValue(), expected[i][j].doubleValue(), i, j)
                    if(!innerAssertion(str, actual[i][j], expected[i][j], delta)) {
                        return false
                    }
                }
            }
        }
        return true
    }

    private static boolean assertion(String message, double actualValue, double expectedValue, double epsilon) {
        boolean result = error(actualValue, expectedValue, epsilon)
        org.hamcrest.MatcherAssert.assertThat(message, result)

        return result
    }

    private static error(double d1, double d2, double epsilon = 0.01) {
        if(d1 == d2) return true
        Math.abs(d1-d2) <= epsilon
    }

    static boolean assertMatrixNotEquals(Matrix expected, Matrix actual, double delta=0.0) {
        assertMatrixEqualsInternals(expected, actual, delta, TestDenseMatrix.&assertionNot)
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

    @Test
    void creationOfMatrix_0x0() {
        def matrix = DenseMatrix.of(0,0)
        assert 0 == matrix.width()
        assert 0 == matrix.height()
    }

    @Test
    void creationOfMatrix_1x1_notNull() {
        def matrix = DenseMatrix.of(1,1)
        assert 0 == matrix[0][0]
    }

    @Test
    void creationOfSquareMatrix_2x2() {
        def matrix = DenseMatrix.of(2,2)
        assert 2 == matrix.width()
        assert 2 == matrix.height()
    }

    @Test
    void creationOfNonSquareMatrix_3x2() {
        def matrix = DenseMatrix.of(3,2)
        assert 3 == matrix.height()
        assert 2 == matrix.width()
    }

    @Test
    void getValidPosition() {
        def matrix = DenseMatrix.of(3,2)
        matrix[2][1]
    }

    @Test
    void getInvalidPosition() {
        def matrix = DenseMatrix.of(3,2)
        shouldFail(ArrayIndexOutOfBoundsException) {
            matrix[3][2]
        }
    }

    @Test
    void givenValidPositionSetValue() {
        def matrix = DenseMatrix.of(3,2)
        matrix[2][1] = 1
        assert 1 == matrix[2][1]
    }

    @Test
    void givenInvalidPositionSetValue() {
        def matrix = DenseMatrix.of(3,3)
        shouldFail(ArrayIndexOutOfBoundsException) {
            matrix[3][3] = 1
        }
    }

    @Test
    void validIdentity() {
        def matrix = DenseMatrix.of(2,2)
        assert DenseMatrix.of([[1,0], [0,1]]) == matrix.identity()
    }

    @Test
    void invalidIdentity() {
        def matrix = DenseMatrix.of(2,3)

        shouldFail(RuntimeException) {
            matrix.identity()
        }
    }

    @Test
    void sumToScalar2() {
        def matrix = DenseMatrix.of(2, 2)

        assert DenseMatrix.of([[2,2],[2,2]]) == matrix + 2
    }

    @Test
    void sumToAnotherMatrix() {
        assert DenseMatrix.of([[0,2],[4,6]]) == DenseMatrix.of([[0,1],[2,3]]) + DenseMatrix.of([[0,1],[2,3]])

    }

    @Test
    void subToScalar2() {
        def matrix = DenseMatrix.of(2, 2)

        assert DenseMatrix.of([[-2,-2],[-2,-2]]) == matrix - 2
    }

    @Test
    void subFromAnotherMatrix() {
        assert DenseMatrix.of([[0,0],[0,0]]) == DenseMatrix.of([[0,1],[2,3]]) - DenseMatrix.of([[0,1],[2,3]])
    }

    @Test
    void multToScalar2() {
        assert DenseMatrix.of([[6,6],[6,6]]) == DenseMatrix.of([[2,2],[2,2]]) * 3
    }

    @Test
    void testMultSameDimension() {
        def m1 = DenseMatrix.of([
            [1, 2, 3],
            [4, 5, 6],
            [7, 8, 9]])

        def m2 = DenseMatrix.of([
            [9, 8, 7],
            [6, 5, 4],
            [3, 2, 1]])

        def m3 = m1 * m2

        def result = DenseMatrix.of([
            [30, 24, 18],
            [84, 69, 54],
            [138, 114, 90]])

        assert result == m3
    }

    @Test
    void testMultInvalidDimension() {
        def m1 = DenseMatrix.of([
            [1, 2, 3],
            [4, 5, 6],
        ])

        def m2 = DenseMatrix.of([
            [9, 8, 7],
            [6, 5, 4],
        ])

        shouldFail() { def m3 = m1 * m2 }
    }

    @Test
    void testMultDifferentDimensionsShouldPass() {
        def m1 = DenseMatrix.of([
            [1, 2, 3],
            [4, 5, 6],
        ])

        def m2 = DenseMatrix.of([
            [9, 8],
            [6, 5],
            [3, 2] ])

        def m3 = m1 * m2

        def result = DenseMatrix.of([
            [30, 24],
            [84, 69],
        ])

        assert result == m3
    }

    @Test
    void testMultDifferentDimensions13x31ShouldPass() {
        def m1 = DenseMatrix.of([[4, 5, 6],])

        def m2 = DenseMatrix.of([[7], [8], [9]])

        def m3 = m1 * m2

        def result = DenseMatrix.of([[122],])

        assert result == m3
    }

    @Test
    void testMultDifferentDimensions23x31ShouldPass() {
        def m1 = DenseMatrix.of([
            [7, 8, 9],
            [4, 5, 6],
        ])

        def m2 = DenseMatrix.of([[7], [8], [9]])

        def m3 = m1 * m2

        def result = DenseMatrix.of([[194], [122],])

        assert result == m3
    }

    @Test
    void divToScalar2() {
        assert DenseMatrix.of([[2,2],[2,2]]) == DenseMatrix.of([[6,6],[6,6]]) / 3
    }

    @Test
    void transposing() {
        assert DenseMatrix.of([[1,2],[3,4],[5,6]]) == DenseMatrix.of([[1,3,5], [2,4,6]]).transpose()
    }

    @Test
    void testGaussJordanExpansion() {
        def m = DenseMatrix.of(0,0)

        def result = m.gaussJordanExpandedMatrix(DenseMatrix.of([
            [1, 2, 3],
            [4, 5, 6],
            [7, 8, 9]]))

        assert DenseMatrix.of([
            [1, 2, 3, 1, 0, 0],
            [4, 5, 6, 0, 1, 0],
            [7, 8, 9, 0, 0, 1]]) == result
    }

    @Test
    void testGetLargestAbsoluteDiagonalElement() {
        def m = DenseMatrix.of(0,0)

        def values = DenseMatrix.of([
            [1, 2, 3, 1, 0, 0],
            [4, 5, 6, 0, 1, 0],
            [7, 8, 9, 0, 0, 1]])

        def q = m.diagonalOrdered(values, 3)

        assertEquals(new Tuple2(2, 9.0), q.poll())
        assertEquals(new Tuple2(1, 5.0), q.poll())
        assertEquals(new Tuple2(0, 1.0), q.poll())
    }

    @Test
    void testGivenNegativeValueGetLargestAbsoluteDiagonalElement() {
        def m = DenseMatrix.of(0,0)

        def values = DenseMatrix.of([
            [1, 2, 3, 1, 0, 0],
            [4, -10, 6, 0, 1, 0],
            [7, 8, 9, 0, 0, 1]] )

        def q = m.diagonalOrdered(values, 3)

        assertEquals(new Tuple2(1, -10.0), q.poll())
        assertEquals(new Tuple2(2, 9.0), q.poll())
        assertEquals(new Tuple2(0, 1.0), q.poll())
    }

    @Test
    void testInverse1() {
        Matrix m = DenseMatrix.of([
            [-1, -1, 3],
            [2, 1, 2],
            [-2, -2, 1]
        ])

        m = m.inverse()

        assertMatrixEquals(DenseMatrix.of([[-1,1,1], 
            [1.2, -1, -1.6], 
            [0.4, 0, -0.2]]), m, 0.1)
    }

    @Test
    void testInverse2() {
        Matrix m = DenseMatrix.of([
            [3, 3.5],
            [3.2, 3.6],
        ])

        m = m.inverse()

        assertMatrixEquals(DenseMatrix.of([[-9, 8.75],
        [8, -7.5]]), m, 0.1)
    }

    @Test
    void testInverse3() {
        Matrix m = DenseMatrix.of([
            [1, 2, 3],
            [0, 1, 4],
            [5, 6, 0]
        ])

        m = m.inverse()
        
        assertMatrixEquals(DenseMatrix.of([
        [-24, 18, 5],
        [20, -15, -4], 
        [-5, 4, 1]]), m, 0.1)
    }
}
