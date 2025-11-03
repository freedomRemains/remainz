CREATE USER 'jluser'@'%' IDENTIFIED BY '12345';
GRANT ALL PRIVILEGES ON JLDB.* TO 'jluser'@'%';
SHOW GRANTS FOR 'jluser'@'%';
SELECT user, host FROM mysql.user;
