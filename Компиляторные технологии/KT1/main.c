#include <stdio.h>
#include <string.h>

// [a-zA-Z0-9.]+'@'[a-zA-Z0-9-]+('.'[a-zA-Z0-9-]+)*
// CT_NAME_CHAR = [a-zA-Z0-9.]
// CT_AT = '@'
// CT_DOT = [a-zA-Z0-9-]
// CT_DOT = '.'

typedef unsigned char u8;

typedef enum char_types {
    CT_UNKNOWN = 0,
    CT_BASE    = 1,
    CT_AT      = 2,
    CT_DOT     = 3,
    CT_MINUS   = 4
} char_types_t;

typedef u8 bool;
#define false 0
#define true  1

const u8 transitions[10][5] = {
        {0, 0, 0, 0, 0},
        {0, 2, 0, 2, 0},
        {0, 2, 4, 3, 0},
        {0, 3, 4, 0, 0},
        {0, 5, 0, 0, 5},
        {0, 5, 0, 7, 6},
        {0, 6, 0, 7, 0},
        {0, 8, 0, 0, 8},
        {0, 8, 0, 7, 9},
        {0, 9, 0, 7, 0}
};

const bool is_final[] = {false, false, false, false, false, true, true, false, true, true};

bool is_base_char(char c)
{
    return ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'));
}

bool is_at(char c)
{
    return (c == '@');
}

bool is_dot(char c)
{
    return (c == '.');
}

bool is_minus(char c)
{
    return (c == '-');
}

char_types_t get_char_type(char c)
{
    if (is_base_char(c))
        return CT_BASE;
    if (is_at(c))
        return CT_AT;
    if (is_dot(c))
        return CT_DOT;
    if (is_minus(c))
        return CT_MINUS;

    return CT_UNKNOWN;
}

bool check_string(const char* str)
{
    u8 state = 1;

    for (u8 i = 0; i < (u8)strlen(str); ++i) {
        state = transitions[state][get_char_type(str[i])];
    }
    return is_final[state];
}

bool get_string(char* str, const u8 MAX_LEN)
{
    bool is_valid;

    fgets(str, MAX_LEN, stdin);

    if (str[strlen(str)-1] == '\n') {
        str[strlen(str)-1] = '\0';
        is_valid = true;
    } else {
        while (getchar() != '\n');
        puts("Error! to much symbols!");
        is_valid = false;
    }

    return is_valid;
}

#define MAX_STR_LEN 100

void find_substrings(char* str, u8 len)
{
    u8 state;

    for (u8 start = 0; start < len - 1; ++start) {
        for (u8 end = 1; end < len; ++end) {
            state = 1;

            for (u8 pos = start; pos <= end; ++pos) {
                state = transitions[state][get_char_type(str[pos])];
                if (state == 0)
                    break;
            }

            if (is_final[state] == true)
                printf("%.*s\n", end - start + 1, str + start);
        }
    }
}

int main(void)
{
    char str[MAX_STR_LEN];
    bool is_str_valid = get_string(str, MAX_STR_LEN);

    if (!is_str_valid)
        return 1;

    is_str_valid = check_string(str);

    if (is_str_valid)
        puts("Main string is correct!");
    else
        puts("Main string is incorrect!");

    find_substrings(str, strlen(str));

    getchar();
    return 0;
}