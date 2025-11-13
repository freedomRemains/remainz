CREATE USER 'remainzuser'@'%' IDENTIFIED BY '12345';
GRANT ALL PRIVILEGES ON REMAINZ_DB.* TO 'remainzuser'@'%';
SHOW GRANTS FOR 'remainzuser'@'%';
SELECT user, host FROM mysql.user;
