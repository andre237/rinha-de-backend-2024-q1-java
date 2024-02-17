create table client_account
(
    id      integer not null
        primary key,
    "limit" bigint  not null,
    balance bigint  not null,
    constraint client_account_v2_check
        check ((balance > 0) OR (abs(balance) <= "limit"))
);

insert into client_account (id, "limit", balance) VALUES
    (1, 100000, 0),
    (2, 80000, 0),
    (3, 2000, 0),
    (4, 10000000, 0),
    (5, 500000, 0);