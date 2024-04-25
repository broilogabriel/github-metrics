CREATE TABLE pull_requests (
    id               bigint      PRIMARY KEY,
    repository_id    bigint      NOT NULL,
    user_id          bigint      NOT NULL,
    merge_commit_sha varchar(40) NOT NULL,
    state            varchar     NOT NULL,
    created_at       timestamp   NOT NULL,
    updated_at       timestamp,
    closed_at        timestamp,
    merged_at        timestamp,
    synchronized_at  timestamp   NOT NULL DEFAULT current_timestamp,
    CONSTRAINT fk_users        FOREIGN KEY(user_id)       REFERENCES users(id),
    CONSTRAINT fk_repositories FOREIGN KEY(repository_id) REFERENCES repositories(id)
);