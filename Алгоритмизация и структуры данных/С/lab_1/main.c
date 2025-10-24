#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef unsigned long u32;
typedef unsigned short u16;
typedef unsigned char u8;

typedef struct user_data user_t;
typedef u8** tick_data;
typedef tick_data* table_data;

typedef u8 bool;
#define true 1
#define false 0

struct user_data {
    bool is_active;
    bool processed;
    u16 idx;
    u32 proc_period_idx;
    u8 priority;
    u8 input_time;
    user_t *next_user;
    u16 periods_num;
    u8 *arr_proc_periods;
};

typedef struct s_queue {
    user_t* first;
    user_t* last;
} queue_t;

#define MAX_PRIORITY_LEVEL 128
typedef struct priority_queue_arr {
    queue_t queues[MAX_PRIORITY_LEVEL];
} pqr_t;

typedef enum table_states {
    TS_OFF        = ' ',
    TS_PROCESSING = 'L',
    TS_INPUT      = 'B',
    TS_WAITING    = 'O'
} table_states_t;

typedef struct s_tick {
    u8 proc_time;
    u8 time;
    u32 idx;
} tick_t;

pqr_t *g_prq = NULL;
pqr_t *g_backup_prq = NULL;
u8 g_users_num = 0;
u8 g_input_time = 5;
u8 g_proc_time = 3;
u32 g_total_time = 0;
u32 g_total_down_time = 0;

#define START_TABLE_TICKS 6

void add_to_queue(queue_t* queue, user_t* new_node);
void pqr_init(pqr_t** pqr);
void pqr_push(pqr_t* pqr, user_t* user);
user_t* pqr_pop_highest(pqr_t* pqr);
bool pqr_is_empty(pqr_t* pqr);
void pqr_clear_proc_flag(pqr_t* pqr);
void pqr_move(pqr_t* pqr, u8 p);
void show_menu();
void process_menu();

u32 count_total_time(queue_t* q)
{
    if (q == NULL)
        return 0;

    user_t* u = q->first;
    u32 val = 0;

    while (u != NULL) {
        for (u16 i = 0; i < u->periods_num; i++)
            val += u->arr_proc_periods[i];
        u = u->next_user;
    }
    return val;
}


user_t* copy_user(const user_t* original) {
    if (!original) return NULL;

    user_t* copy = malloc(sizeof(user_t));
    copy->is_active = original->is_active;
    copy->processed = original->processed;
    copy->idx = original->idx;
    copy->proc_period_idx = original->proc_period_idx;
    copy->input_time = original->input_time;
    copy->priority = original->priority;
    copy->next_user = NULL;
    copy->periods_num = original->periods_num;

    copy->arr_proc_periods = malloc(copy->periods_num * sizeof(u8));
    memcpy(copy->arr_proc_periods, original->arr_proc_periods, copy->periods_num * sizeof(u8));

    return copy;
}

pqr_t* copy_pqr(const pqr_t* original) {
    if (!original) return NULL;

    pqr_t* copy;
    pqr_init(&copy);

    for (int p = 0; p < MAX_PRIORITY_LEVEL; p++) {
        user_t* current = original->queues[p].first;
        while (current) {
            user_t* user_copy = copy_user(current);
            pqr_push(copy, user_copy);
            current = current->next_user;
        }
    }

    return copy;
}

queue_t* copy_pqr_to_fifo(const pqr_t* pqr) {
    if (!pqr) return NULL;

    queue_t* fifo = malloc(sizeof(queue_t));
    fifo->first = NULL;
    fifo->last = NULL;

    for (int p = MAX_PRIORITY_LEVEL - 1; p >= 0; p--) {
        user_t* current = pqr->queues[p].first;
        while (current) {
            user_t* user_copy = copy_user(current);
            add_to_queue(fifo, user_copy);
            current = current->next_user;
        }
    }

    return fifo;
}

void clear_queue(pqr_t* prq)
{
    if (prq) {
        for (u32 p = 0; p < MAX_PRIORITY_LEVEL; p++) {
            while (prq->queues[p].first) {
                user_t* temp = prq->queues[p].first;
                prq->queues[p].first = prq->queues[p].first->next_user;
                if (temp->arr_proc_periods)
                    free(temp->arr_proc_periods);
                free(temp);
            }
            prq->queues[p].last = NULL;
        }
        free(prq);
        prq = NULL;
    }
}

void clear_queues() {
    clear_queue(g_prq);
    clear_queue(g_backup_prq);
}

void set_users_num() {
    printf("Enter number of users (1-20): ");
    int num;
    scanf("%d", &num);
    if (num < 1 || num > 20) {
        printf("Invalid number! Using default (3)\n");
        g_users_num = 3;
    } else {
        g_users_num = num;
    }
    printf("Number of users set to: %d\n", g_users_num);
}

void set_users_data() {
    if (g_users_num == 0) {
        printf("Please set number of users first!\n");
        return;
    }

    clear_queues();
    pqr_init(&g_prq);

    for (int i = 0; i < g_users_num; i++) {
        user_t* user = malloc(sizeof(user_t));
        user->is_active = false;
        user->processed = false;
        user->idx = i;
        user->proc_period_idx = 0;
        user->input_time = 0;
        user->next_user = NULL;

        printf("\n--- User %d ---\n", i + 1);
        printf("Priority (0-127): ");
        int priority;
        scanf("%d", &priority);
        if (priority < 0 || priority >= MAX_PRIORITY_LEVEL) {
            printf("Invalid priority! Using 0\n");
            priority = 0;
        }
        user->priority = priority;

        printf("Number of processing periods (1-10): ");
        int periods;
        scanf("%d", &periods);
        if (periods < 1 || periods > 10) {
            printf("Invalid periods! Using 1\n");
            periods = 1;
        }
        user->periods_num = periods;

        user->arr_proc_periods = malloc(user->periods_num * sizeof(u8));

        for (int j = 0; j < user->periods_num; j++) {
            printf("Period %d processing time (1-20): ", j + 1);
            int time;
            scanf("%d", &time);
            if (time < 1 || time > 20) {
                printf("Invalid time! Using 1\n");
                time = 1;
            }
            user->arr_proc_periods[j] = time;
        }

        pqr_push(g_prq, user);
    }

    printf("\nUsers data set successfully!\n");
}

void set_input_time() {
    printf("Enter input time (0-20): ");
    int time;
    scanf("%d", &time);
    if (time < 0 || time > 20) {
        printf("Invalid time! Using default (5)\n");
        g_input_time = 5;
    } else {
        g_input_time = time;
    }
    printf("Input time set to: %d\n", g_input_time);
}

void set_processing_time() {
    printf("Enter processing time per tick (1-10): ");
    int time;
    scanf("%d", &time);
    if (time < 1 || time > 10) {
        printf("Invalid time! Using default (3)\n");
        g_proc_time = 3;
    } else {
        g_proc_time = time;
    }
    printf("Processing time set to: %d\n", g_proc_time);
}

void show_all_data() {
    printf("\n=== Current Configuration ===\n");
    printf("Number of users: %d\n", g_users_num);
    printf("Input time: %d\n", g_input_time);
    printf("Processing time per tick: %d\n", g_proc_time);

    if (!g_prq || g_users_num == 0) {
        printf("No users data set!\n");
        return;
    }

    printf("\n=== Users Data ===\n");
    for (int p = MAX_PRIORITY_LEVEL - 1; p >= 0; p--) {
        user_t* current = g_prq->queues[p].first;
        while (current) {
            printf("User %d: Priority=%d, Periods=%d, Times=[",
                   current->idx + 1, current->priority, current->periods_num);
            for (int i = 0; i < current->periods_num; i++) {
                printf("%d", current->arr_proc_periods[i]);
                if (i < current->periods_num - 1) printf(", ");
            }
            printf("]\n");
            current = current->next_user;
        }
    }
}

void fill_tick(tick_data tick, u16 users_num, u8 tick_len)
{
    for (u16 i = 0; i < users_num; ++i)
        for (u8 j = 0; j < tick_len; ++j)
            tick[i][j] = TS_OFF;
}

table_data gen_base_table(u16 users_num, u8 tick_len)
{
    table_data table = calloc(START_TABLE_TICKS, sizeof(tick_data));

    for (u32 i = 0; i < START_TABLE_TICKS; ++i) {
        table[i] = calloc(users_num, sizeof(u8*));
        for (u32 j = 0; j < users_num; ++j) {
            table[i][j] = calloc(tick_len, sizeof(u8));
        }
        fill_tick(table[i], users_num, tick_len);
    }

    return table;
}

table_data add_ticks_to_table(table_data table, u8 tick_len, u16 users_num,
                              u16 old_ticks_num, u16 new_ticks_num)
{
    table_data new_table = calloc(new_ticks_num, sizeof(tick_data));

    for (u32 i = 0; i < old_ticks_num; ++i)
        new_table[i] = table[i];

    for (u32 i = old_ticks_num; i < new_ticks_num; ++i) {
        new_table[i] = calloc(users_num, sizeof(u8*));
        for (u32 j = 0; j < users_num; ++j)
            new_table[i][j] = calloc(tick_len, sizeof(u8));
        fill_tick(new_table[i], users_num, tick_len);
    }

    free(table);
    return new_table;
}

void free_table(table_data table, u16 user_num, u16 tick_num)
{
    if (table == NULL)
        return;

    for (u32 i = 0; i < tick_num; ++i) {
        for (u32 j = 0; j < user_num; ++j)
            free(table[i][j]);
        free(table[i]);
    }
    free(table);
}

void add_to_queue(queue_t* queue, user_t* new_node)
{
    new_node->next_user = NULL;

    if (queue->first == NULL) {
        queue->first = new_node;
        queue->last  = new_node;
    } else {
        queue->last->next_user = new_node;
        queue->last = new_node;
    }
}

user_t* remove_from_queue(queue_t* queue)
{
    if (queue->first == NULL)
        return NULL;

    user_t* removed_user = queue->first;
    queue->first = queue->first->next_user;

    if (queue->first == NULL)
        queue->last = NULL;

    removed_user->next_user = NULL;
    return removed_user;
}

void delete_user(user_t* user)
{
    if (!user)
        return;
    free(user->arr_proc_periods);
    free(user);
}

void free_queue(queue_t* queue)
{
    if (!queue) return;

    while (queue->first) {
        user_t* temp = remove_from_queue(queue);
        delete_user(temp);
    }
    free(queue);
}

void pqr_init(pqr_t** pqr)
{
    *pqr = malloc(sizeof(pqr_t));
    if (!*pqr)
        return;
    for (int i = 0; i < MAX_PRIORITY_LEVEL; ++i) {
        (*pqr)->queues[i].first = NULL;
        (*pqr)->queues[i].last  = NULL;
    }
}

void pqr_move(pqr_t* pqr, u8 p)
{
    if (p >= MAX_PRIORITY_LEVEL) return;
    user_t* user = remove_from_queue(&pqr->queues[p]);
    if (user)
        add_to_queue(&pqr->queues[p], user);
}

void pqr_push(pqr_t* pqr, user_t* user)
{
    if (!pqr || !user)
        return;

    u8 p = user->priority;
    if (p >= MAX_PRIORITY_LEVEL) p = MAX_PRIORITY_LEVEL - 1;
    add_to_queue(&pqr->queues[p], user);
}

user_t* pqr_pop_highest(pqr_t* pqr)
{
    if (!pqr)
        return NULL;

    for (int p = MAX_PRIORITY_LEVEL - 1; p >= 0; --p)
        if (pqr->queues[p].first != NULL && !pqr->queues[p].first->processed)
            return remove_from_queue(&pqr->queues[p]);
    return NULL;
}

bool pqr_is_empty(pqr_t* pqr)
{
    if (!pqr)
        return true;

    for (u8 p = 0; p < MAX_PRIORITY_LEVEL; ++p) {
        if (pqr->queues[p].first != NULL)
            return false;
    }
    return true;
}

void pqr_clear_proc_flag(pqr_t* pqr)
{
    if (!pqr)
        return;

    for (u8 p = 0; p < MAX_PRIORITY_LEVEL; ++p) {
        user_t* cur = pqr->queues[p].first;
        while (cur) {
            cur->processed = false;
            cur = cur->next_user;
        }
    }
}

void output_table(table_data table, u32 tick_num, u16 users_num, u8 tick_len, u8* downtime_arr)
{
    if (table == NULL) return;

    const int TICKS_PER_LINE = 15;

    printf("\n=== EXECUTION TIMELINE ===\n");
    printf("Legend: L=Processing, B=Input, O=Waiting, ' '=Off, *=CPU_idle\n\n");
    u8 x = 0;
    for (u32 start_tick = 0; start_tick < tick_num; start_tick += TICKS_PER_LINE) {
        u32 end_tick = (start_tick + TICKS_PER_LINE < tick_num) ?
                       start_tick + TICKS_PER_LINE : tick_num;

        if (tick_num > TICKS_PER_LINE) {
            printf("--- Ticks %u-%u ---\n", start_tick, end_tick - 1);
        }

        printf("Down ");
        for (u32 j = start_tick; j < end_tick; ++j) {
            for (u8 i = 0; i < downtime_arr[x]; ++i)
                putchar(' ');

            for (u8 i = 0; i < tick_len - downtime_arr[x]; ++i)
                putchar('*');

            ++x;
            printf("|");
        }
        printf("\n");

        printf("User ");
        for (u32 j = start_tick; j < end_tick; ++j) {
            printf("%*u", tick_len, j % 100);
            printf("|");
        }
        printf("\n");

        printf("-----");
        for (u32 j = start_tick; j < end_tick; ++j) {
            for (u8 k = 0; k < tick_len; ++k)
                printf("-");
            printf("+");
        }
        printf("\n");

        for (u16 i = 0; i < users_num; ++i) {
            printf("  %2d ", i);
            for (u32 j = start_tick; j < end_tick; ++j) {
                for (u8 k = 0; k < tick_len; ++k)
                    putchar(table[j][i][k]);
                putchar('|');
            }
            printf("\n");
        }
        printf("\n");
    }

    printf("=== SUMMARY ===\n");
    printf("Total execution time: %u ticks\n", tick_num);
    printf("Number of users: %u\n", users_num);
    printf("Time units per tick: %u\n", tick_len);
    printf("Total time units: %lu\n", tick_num * tick_len);
    printf("efficiency = %f\n", (double)g_total_time / (tick_num * tick_len));
}

void proc_processing_time(table_data table, tick_t* tick, user_t* user, u8* user_proc, u8* proc_time)
{
    if (*proc_time == tick->proc_time)
        while (*user_proc > 0 && tick->time < tick->proc_time && *proc_time > 0) {
            table[tick->idx][user->idx][tick->time] = TS_PROCESSING;
            user->is_active = true;

            ++tick->time;
            --*user_proc;
            --*proc_time;
        }
}

bool should_delete_user(const user_t* user, const u8* user_proc)
{
    return (user->proc_period_idx == user->periods_num - 1 && *user_proc == 0);
}

static void process_input_time(table_data table, u8 user_proc, tick_t* tick, user_t* user, u8* user_input, u8 input_time)
{
    if (user_proc == 0) {
        if (*user_input == 0 && user->proc_period_idx < user->periods_num)
            *user_input = input_time;

        while (*user_input > 0 && tick->time < tick->proc_time) {
            table[tick->idx][user->idx][tick->time] = TS_INPUT;
            ++tick->time;
            --*user_input;
        }

        if (*user_input == 0 && user->proc_period_idx < user->periods_num)
            ++user->proc_period_idx;
    }
}

void process_waiting_time(table_data table, tick_t* tick, const user_t* user)
{
    if (user->is_active)
        while (tick->time < tick->proc_time) {
            table[tick->idx][user->idx][tick->time] = TS_WAITING;
            ++tick->time;
        }
}

bool handle_user_for_tick_priority(table_data table, tick_t* tick, user_t* user,
                                   u8 INPUT_TIME, u8* proc_time)
{
    u8* user_proc = &user->arr_proc_periods[user->proc_period_idx];

    proc_processing_time(table, tick, user, user_proc, proc_time);

    if (should_delete_user(user, user_proc))
        return true;

    process_input_time(table, *user_proc, tick, user, &user->input_time, INPUT_TIME);
    process_waiting_time(table, tick, user);

    return false;
}

bool handle_user_for_tick_fifo(table_data table, tick_t* tick, user_t* user,
                               u8 INPUT_TIME, u8* proc_time)
{
    u8* user_proc = &user->arr_proc_periods[user->proc_period_idx];

    proc_processing_time(table, tick, user, user_proc, proc_time);

    if (should_delete_user(user, user_proc))
        return true;

    process_input_time(table, *user_proc, tick, user, &user->input_time, INPUT_TIME);
    process_waiting_time(table, tick, user);

    return false;
}

void move_node(queue_t* queue)
{
    user_t* temp = remove_from_queue(queue);
    if (temp) add_to_queue(queue, temp);
}

void output_min(double efficiency, u32 downtime_num)
{
    printf("%*.0f%%|%*lu", 4, efficiency * 100, 3, downtime_num);
}

void priority_users_data_processing(pqr_t* pqr, const u8 USERS_NUM, const u8 TICK_LEN, const u8 INPUT_TIME, bool OUTPUT)
{
    if (OUTPUT)
        printf("=== PRIORITY SCHEDULING ALGORITHM ===\n");

    tick_t tick = { .proc_time = TICK_LEN, .time = 0, .idx = 0 };
    u32 alloc_ticks = START_TABLE_TICKS;
    u8 down_time_arr[1024] = {0};
    u32 total_down_time = 0;
    u8 cur_users_num;
    u8 users_num = USERS_NUM;
    u8 proc_time;
    user_t* user;
    u8 low_pr = 0;

    table_data table = gen_base_table(users_num, tick.proc_time);

    while (!pqr_is_empty(pqr)) {
        proc_time = TICK_LEN;
        cur_users_num = users_num;

        for (u8 passed_users = 0; passed_users < cur_users_num; ++passed_users) {
            if (tick.idx + 1 == alloc_ticks) {
                table = add_ticks_to_table(table, tick.proc_time, USERS_NUM, alloc_ticks, alloc_ticks << 1);
                alloc_ticks = alloc_ticks << 1;
            }

            tick.time = 0;

            user = pqr_pop_highest(pqr);
            if (user == NULL)
                break;

            bool finished = handle_user_for_tick_priority(table, &tick, user, INPUT_TIME, &proc_time);
            low_pr = user->priority;

            if (finished) {
                delete_user(user);
                --users_num;
            } else {
                user->processed = true;
                pqr_push(pqr, user);
            }
        }
        pqr_clear_proc_flag(pqr);
        down_time_arr[tick.idx] = tick.proc_time - proc_time;
        total_down_time += proc_time;
        g_total_down_time = total_down_time;

        if (!pqr_is_empty(pqr)) {
            if (cur_users_num == users_num)
                pqr_move(pqr, low_pr);
            ++tick.idx;
        }
    }

    if (OUTPUT) {
        putchar('\n');
        output_table(table, ++tick.idx, USERS_NUM, TICK_LEN, down_time_arr);
    }
    else
        output_min((double)g_total_time / (tick.proc_time*tick.idx), total_down_time);
    free_table(table, USERS_NUM, tick.idx);
}

void fifo_users_data_processing(queue_t* queue, const u8 USERS_NUM, const u8 TICK_LEN, const u8 INPUT_TIME, bool OUTPUT)
{
    if (OUTPUT)
        printf("=== FIFO SCHEDULING ALGORITHM ===\n");

    tick_t tick = { .proc_time = TICK_LEN, .time = 0, .idx = 0 };
    u32 alloc_ticks = START_TABLE_TICKS;
    u8 cur_users_num;
    u8 users_num = USERS_NUM;
    u8 proc_time;
    table_data table = gen_base_table(users_num, tick.proc_time);
    u8 down_time_arr[1024] = {0};
    u32 total_down_time = 0;

    while (queue->first != NULL) {
        proc_time = TICK_LEN;
        cur_users_num = users_num;

        for (u8 passed_users = 0; passed_users < cur_users_num; ++passed_users) {
            if (tick.idx + 1 == alloc_ticks) {
                table = add_ticks_to_table(table, tick.proc_time, USERS_NUM, alloc_ticks, alloc_ticks << 1);
                alloc_ticks = alloc_ticks << 1;
            }
            tick.time = 0;
            user_t* user = remove_from_queue(queue);
            if (user == NULL)
                break;

            bool finished = handle_user_for_tick_fifo(table, &tick, user, INPUT_TIME, &proc_time);

            if (!finished)
                add_to_queue(queue, user);
            else {
                delete_user(user);
                --users_num;
            }
        }
        down_time_arr[tick.idx] = tick.proc_time - proc_time;
        total_down_time += down_time_arr[tick.idx];

        if (queue->first != NULL) {
            if (cur_users_num == users_num)
                move_node(queue);
            ++tick.idx;
        }
    }

    putchar('\n');
    if (OUTPUT)
        output_table(table, ++tick.idx, USERS_NUM, TICK_LEN, down_time_arr);
    else
        output_min((double)g_total_time / tick.proc_time*tick.idx, total_down_time);
    free_table(table, USERS_NUM, tick.idx);
}

void start_processing() {
    g_backup_prq = copy_pqr(g_prq);
    queue_t* fifo_queue = copy_pqr_to_fifo(g_backup_prq);
    g_total_time = count_total_time(fifo_queue);

    priority_users_data_processing(g_prq, g_users_num, g_proc_time, g_input_time, true);

    printf("\n");

    fifo_users_data_processing(fifo_queue, g_users_num, g_proc_time, g_input_time, true);

    free_queue(fifo_queue);

    if (g_prq) {
        for (int p = 0; p < MAX_PRIORITY_LEVEL; p++) {
            while (g_prq->queues[p].first) {
                user_t* temp = g_prq->queues[p].first;
                g_prq->queues[p].first = g_prq->queues[p].first->next_user;
                if (temp->arr_proc_periods) free(temp->arr_proc_periods);
                free(temp);
            }
            g_prq->queues[p].last = NULL;
        }
        free(g_prq);
        g_prq = NULL;
    }

    g_prq = g_backup_prq;
    g_backup_prq = NULL;

    printf("\n--- Processing completed! Queue restored for next run ---\n");
}

void show_menu() {
    printf("\n-------------------------------\n");
    printf("| 1. Set number of users      |\n");
    printf("| 2. Set users data           |\n");
    printf("|-----------------------------|\n");
    printf("| 3. Set input time           |\n");
    printf("| 4. Set processing time      |\n");
    printf("| 5. See all data             |\n");
    printf("| 6. Start processing         |\n");
    printf("|-----------------------------|\n");
    printf("| 7. Reset queue              |\n");
    printf("| 8. Output efficiency        |\n");
    printf("| 0. Exit                     |\n");
    printf("-------------------------------\n");
    printf("Your choice: ");
}

void output_efficiency_table()
{
    g_backup_prq = copy_pqr(g_prq);
    queue_t* fifo_queue = copy_pqr_to_fifo(g_backup_prq);
    g_total_time = count_total_time(fifo_queue);

    for (u32 i = 1; i < 11; i++) {
        for (u32 j = 0; j < 10; j++) {
            g_input_time = j;
            g_proc_time = i;
            priority_users_data_processing(g_backup_prq, g_users_num, g_proc_time,
                                           g_input_time, false);
            g_backup_prq = copy_pqr(g_prq);
        }
        putchar('\n');
    }

    free_queue(fifo_queue);

    g_prq = g_backup_prq;
    g_backup_prq = NULL;

    printf("\n--- Processing completed! Queue restored for next run ---\n");
}

void process_menu() {
    int choice;

    while (1) {
        show_menu();
        scanf("%d", &choice);

        switch (choice) {
            case 1:
                clear_queues();
                set_users_num();
                break;
            case 2:
                clear_queues();
                set_users_data();
                break;
            case 3:
                set_input_time();
                break;
            case 4:
                set_processing_time();
                break;
            case 5:
                show_all_data();
                break;
            case 6:
                start_processing();
                break;
            case 7:
                clear_queues();
                g_users_num = 0;
                printf("Queue reset successfully!\n");
                break;
            case 8:
                output_efficiency_table();
                break;
            case 0:
                printf("Exiting...\n");
                clear_queues();
                return;
            default:
                printf("Invalid choice! Try again.\n");
        }
    }
}

void set_default_queue() {
    printf("Setting up default queue with predefined data...\n");

    clear_queues();
    pqr_init(&g_prq);
    g_users_num = 6;

    u8 default_data[6][11] = {
            {6, 4, 3, 4, 6, 8, 5, 9, 7, 0, 0},
            {3, 2, 1, 6, 8, 9, 7, 4, 3, 1, 0},
            {2, 1, 2, 3, 1, 6, 1, 8, 9, 7, 0},
            {5, 3, 5, 6, 6, 7, 8, 2, 1, 8, 0},
            {5, 4, 3, 2, 1, 8, 7, 6, 3, 2, 1},
            {5, 9, 3, 1, 2, 9, 7, 6, 4, 2, 1}
    };

    u8 periods_count[6] = {9, 10, 10, 10, 11, 11};
    u8 default_priorities[6] = {3, 2, 2, 2, 1, 1};

    for (int i = 0; i < 6; i++) {
        user_t* user = malloc(sizeof(user_t));
        user->is_active = false;
        user->processed = false;
        user->idx = i;
        user->proc_period_idx = 0;
        user->input_time = 0;
        user->next_user = NULL;
        user->priority = default_priorities[i];
        user->periods_num = periods_count[i];

        user->arr_proc_periods = malloc(user->periods_num * sizeof(u8));
        for (int j = 0; j < user->periods_num; j++) {
            user->arr_proc_periods[j] = default_data[i][j];
        }

        pqr_push(g_prq, user);
    }
    printf("Default queue setup completed!\n");
}

int main(void)
{
    printf("Task Scheduling Simulator\n");
    set_default_queue();
    process_menu();
    return 0;
}