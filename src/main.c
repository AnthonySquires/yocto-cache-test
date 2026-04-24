#include <stdio.h>
#include "version.h"

int main(void)
{
    printf("yocto-cache-test %s built %s\n", GIT_COMMIT, COMPILE_DATE);
    return 0;
}
