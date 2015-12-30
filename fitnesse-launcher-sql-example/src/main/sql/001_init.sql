
CREATE TABLE example_table (
    id        INT          NOT NULL PRIMARY KEY,
    who       VARCHAR(20)  NOT NULL,
    when_done TIMESTAMP    NOT NULL,
    place     VARCHAR(20)  DEFAULT NULL,
    implement VARCHAR(20)  DEFAULT NULL
);

INSERT INTO example_table VALUES (1, 'Prof Plum', CURRENT_TIMESTAMP, 'Library', 'Candlestick');
