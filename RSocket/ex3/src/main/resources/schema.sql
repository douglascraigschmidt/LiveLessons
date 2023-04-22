create table if not exists QUOTE (
    id INT PRIMARY KEY AUTO_INCREMENT,
    play varchar(256) NOT NULL,
    quote text NOT NULL,
    sentiment text DEFAULT NULL
);
