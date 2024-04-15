DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
                        `id` int(10) NOT NULL AUTO_INCREMENT,
                        `userID` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
                        `password` varchar(255) DEFAULT NULL,
                        `email` varchar(255) DEFAULT NULL,
                        `phone` varchar(255) DEFAULT NULL,
                        `isadmin` int(10) NOT NULL,
                        `user_name` varchar(255) DEFAULT NULL,
                        `picture` varchar(255) DEFAULT NULL,
                        PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8;
INSERT INTO `user` VALUES ('1', 'user1', 'password', '', '', '0', 'test', '');

DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
                           `messageID` int(11) NOT NULL AUTO_INCREMENT,
                           `state` int(11) DEFAULT NULL,
                           `userID` varchar(25) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
                           `content` varchar(5000) DEFAULT NULL,
                           `time` datetime DEFAULT NULL,
                           PRIMARY KEY (`messageID`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8;
