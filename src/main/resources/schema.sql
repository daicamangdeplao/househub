create extension if not exists vector;

create table if not exists knowledge_base (
    id bigint generated always as identity primary key,
    uuid uuid not null,
    topic varchar(128) not null,
    embedding vector(1536) not null,
    created_at timestamptz default now(),
    unique (uuid)
);

create index if not exists knowledge_base_embedding_idx
    on knowledge_base
    using ivfflat (embedding vector_cosine_ops)
    with (lists = 100);
