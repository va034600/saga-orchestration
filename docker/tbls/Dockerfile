FROM postgres:16-bookworm

COPY --from=ghcr.io/k1low/tbls:latest /usr/bin/tbls /usr/local/bin/tbls

RUN apt-get update \
    && apt-get install -y --no-install-recommends python3 pandoc \
    && rm -rf /var/lib/apt/lists/*

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
