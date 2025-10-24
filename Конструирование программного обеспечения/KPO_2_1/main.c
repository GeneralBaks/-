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

void replace_max_and_min(int mtrx[MATRIX_SIZE][MATRIX_SIZE])
{
    for (u8 i = 0; i < MATRIX_SIZE; ++i) {
        int* min = &mtrx[i][0];
        int* max = &mtrx[i][0];
        for (u8 j = 1; j < MATRIX_SIZE; ++j) {
            int* var = &mtrx[i][j];
            if (*var < *min)
                min = var;
            else if (*var > *max)
                max = var;
        }
        if (min != max) {
            int temp = *min;
            *min = *max;
            *max = temp;
        }
    }
}

int main(void)
{
    int array[MATRIX_SIZE][MATRIX_SIZE] = {0};
    fill_matrix(array);
    output_matrix(array);
    replace_max_and_min(array);
    putchar('\n');
    output_matrix(array);
    return 0;
}
