create table if not exists QUOTE (
    id SERIAL primary key,
    type INTEGER, 
    quote varchar(1024) NOT NULL
);
