DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
                           `messageID` int(11) NOT NULL AUTO_INCREMENT,
                           `state` int(11) DEFAULT NULL,
                           `userID` varchar(25) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
                           `content` varchar(5000) DEFAULT NULL,
                           `time` datetime DEFAULT NULL,
                           PRIMARY KEY (`messageID`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8;
INSERT INTO `message` (`messageID`, `state`, `userID`, `content`, `time`)
VALUES (1, 3, 'user1', 'message_content', NOW());
