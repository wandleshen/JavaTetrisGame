create table `user` (
    `username` varchar(255) UNIQUE,
    `password` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`username`)
);
create table `battles` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `player1` varchar(255) DEFAULT NULL,
    `player2` varchar(255) DEFAULT NULL,
    `player1score` int(11) DEFAULT NULL,
    `player2score` int(11) DEFAULT NULL,
    `winner` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`)
);