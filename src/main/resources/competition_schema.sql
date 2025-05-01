-- 竞赛表
CREATE TABLE IF NOT EXISTS `competition` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '竞赛ID',
  `name` varchar(255) NOT NULL COMMENT '竞赛名称',
  `creator_id` int NOT NULL COMMENT '创建人ID',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `status` int DEFAULT '0' COMMENT '状态:0未开始,1进行中,2已结束',
  `description` text COMMENT '竞赛描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int DEFAULT '0' COMMENT '是否删除:0否,1是',
  PRIMARY KEY (`id`),
  KEY `idx_creator` (`creator_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞赛表';

-- 竞赛参与者表
CREATE TABLE IF NOT EXISTS `competition_participant` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `competition_id` int NOT NULL COMMENT '竞赛ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `join_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '参与时间',
  `score` int DEFAULT '0' COMMENT '得分',
  `rank` int DEFAULT NULL COMMENT '排名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_competition_user` (`competition_id`,`user_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_competition` (`competition_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞赛参与者表';

-- 竞赛题目关联表
CREATE TABLE IF NOT EXISTS `competition_question` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `competition_id` int NOT NULL COMMENT '竞赛ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `score` int DEFAULT '100' COMMENT '题目分值',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_competition_question` (`competition_id`,`question_id`),
  KEY `idx_competition` (`competition_id`),
  KEY `idx_question` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞赛题目关联表'; 