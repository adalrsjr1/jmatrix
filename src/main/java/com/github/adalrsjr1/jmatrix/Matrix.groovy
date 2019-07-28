package com.github.adalrsjr1.jmatrix

interface Matrix<T> {
    
    int width()
    
    int height()
    
    Matrix identity()
    
    def getAt(int i)
    
    Matrix plus(T scalar)

    Matrix plus(Matrix other) 
    
    Matrix minus(T scalar)
    
    Matrix minus(Matrix other)
    
    Matrix multiply(T scalar)
    
    Matrix multiply(Matrix other)
    
    Matrix div(T scalar)
    
    Matrix div(Matrix other)
    
    Matrix inverse()
    
    Matrix transpose()
    
    double determinant()
    
    String prettyToString() 
}
