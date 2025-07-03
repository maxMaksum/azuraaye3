// Minimal public domain SHA256 implementation header
// Source: https://github.com/B-Con/crypto-algorithms (public domain)
#ifndef SHA256_H
#define SHA256_H

#include <stdint.h>
#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef struct {
    uint8_t data[64];
    uint32_t datalen;
    unsigned long long bitlen;
    uint32_t state[8];
} SHA256_CTX;

void sha256_init(SHA256_CTX *ctx);
void sha256_update(SHA256_CTX *ctx, const uint8_t data[], size_t len);
void sha256_final(SHA256_CTX *ctx, uint8_t hash[]);
void sha256(const uint8_t* data, size_t len, uint8_t* out_hash);

#ifdef __cplusplus
}
#endif

#endif // SHA256_H
