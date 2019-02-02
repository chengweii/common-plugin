CREATE TABLE `tcc_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `transaction_id` int(11) NOT NULL DEFAULT '1000' COMMENT '补偿事务业务主键',
  `compensate_action_clz` varchar(128) NOT NULL DEFAULT '' COMMENT '事务补偿动作类型',
  `actionSerialNo` varchar(20) DEFAULT NULL COMMENT '事务补偿动作序号',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '事务状态，1：待执行或执行中，2：待重试或重试中，3：最终执行失败',
  `transaction_data` varchar(8000) NOT NULL DEFAULT '' COMMENT '事务依赖数据',
  `retry_times` int(11) NOT NULL DEFAULT '0' COMMENT '已重试次数',
  `next_at` datetime NOT NULL COMMENT '事务下次重试时间',
  `execute_at` datetime NOT NULL COMMENT '事务最近执行时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_transaction_id` (`transaction_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='事务补偿记录表';