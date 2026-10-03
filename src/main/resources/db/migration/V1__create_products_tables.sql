create table products (
    id           bigint generated always as identity primary key,
    external_id  bigint unique,
    title        text not null,
    vendor       text,
    product_type text,
    deleted_at   timestamptz
);

create table product_variants (
    id                 bigint generated always as identity primary key,
    product_id         bigint not null references products(id) on delete cascade,
    title              text not null,
    price              numeric(10,2) not null,
    featured_image_src text,
    available          boolean not null default true
);

create index product_variants_product_id_idx on product_variants (product_id);
