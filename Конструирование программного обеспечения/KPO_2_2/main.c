#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#define MATRIX_SIZE 6
#define MAX_NUM     100
typedef unsigned char u8;

void fill_matrix(int mtrx[MATRIX_SIZE][MATRIX_SIZE])
{
    srand(time(NULL));
    for (u8 i = 0; i < MATRIX_SIZE; ++i) {
        for (u8 j = 0; j < MATRIX_SIZE; ++j) {
            mtrx[i][j] = rand() % MAX_NUM;
        }
    }
}

void output_matrix(int mtrx[MATRIX_SIZE][MATRIX_SIZE])
{
    for (u8 i = 0; i < MATRIX_SIZE; ++i) {
        printf("{ ");
        for (u8 j = 0; j < MATRIX_SIZE; ++j) {
            printf("%2d ", mtrx[i][j]);
        }
        printf("}\n");
    }
}

int* find_min(int col, int mtrx[MATRIX_SIZE][MATRIX_SIZE])
{
    int* min = &mtrx[0][col];
    for (u8 i = 1; i < MATRIX_SIZE; ++i) {
        int* var = &mtrx[i][col];
        if (*var < *min)
            min = var;
    }
    return min;
}

void move_to_diagonal(int mtrx[MATRIX_SIZE][MATRIX_SIZE])
{
    int n = MATRIX_SIZE - 1;
    for (u8 i = 0; i < MATRIX_SIZE; ++i) {
        int* min = find_min(i, mtrx);

        if (min != &mtrx[n][i]) {
            int temp = mtrx[n][i];
            mtrx[n][i] = *min;
            *min = temp;
        }
        --n;
    }
}

int main(void)
{
    int array[MATRIX_SIZE][MATRIX_SIZE] = {0};
    fill_matrix(array);
    output_matrix(array);
    move_to_diagonal(array);
    putchar('\n');
    output_matrix(array);
    return 0;
}
