-- oads 城镇户外广告与店招牌匾设置安全监管 -- schema (chen-026)
-- 列名与基线实体契约（@TableName/@TableField）逐列对齐，改列必须同步实体。
-- 库：chen_026

CREATE TABLE IF NOT EXISTS t_oads_area_book (
  id bigint NOT NULL COMMENT '主键',
  area_no varchar(64) DEFAULT NULL COMMENT '属地段册代号',
  area_name varchar(128) DEFAULT NULL COMMENT '辖段全称',
  area_kind varchar(32) DEFAULT NULL COMMENT '辖段类别',
  road_name varchar(128) DEFAULT NULL COMMENT '属地一直写到门牌（澜原省—江水市—区—街巷）',
  align_kind varchar(32) DEFAULT NULL COMMENT '来路（市里段册下发／本局自录）',
  status int DEFAULT NULL COMMENT '段册情形 0在册 1已撤出',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='属地段册底册';

CREATE TABLE IF NOT EXISTS t_oads_check_row (
  id bigint NOT NULL COMMENT '主键',
  batch_no varchar(64) DEFAULT NULL COMMENT '集中更换批次码',
  row_no int DEFAULT NULL COMMENT '原报行次',
  item_code varchar(64) DEFAULT NULL COMMENT '被对上的点位码',
  qty decimal(12,2) DEFAULT NULL COMMENT '本行应更换处数',
  chk_num decimal(12,2) DEFAULT NULL COMMENT '本行核验合格处数',
  put_kind varchar(32) DEFAULT NULL COMMENT '来路（区级报上来／市局自核两条来路）',
  status int DEFAULT NULL COMMENT '行落地情形 0未勾 1已收下 2已退回',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='集中更换核验行';

CREATE TABLE IF NOT EXISTS t_oads_gov_flow (
  id bigint NOT NULL COMMENT '主键',
  biz_no varchar(64) DEFAULT NULL COMMENT '点位监管办件流转单号',
  stage int DEFAULT NULL COMMENT '当前关次 0..3（申报/核准/设置/验收）',
  status int DEFAULT NULL COMMENT '单子走到哪一关 0未起 1在办 2已封住',
  content varchar(255) DEFAULT NULL COMMENT '一关一记',
  last_action varchar(64) DEFAULT NULL COMMENT '最近一次挪关动作',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='点位监管办件流转单';

CREATE TABLE IF NOT EXISTS t_oads_notice_item (
  id bigint NOT NULL COMMENT '主键',
  item_no varchar(64) DEFAULT NULL COMMENT '届期催办与巡查条目代号',
  due_at datetime DEFAULT NULL COMMENT '该发催办那一一日（落库为 datetime）',
  amount decimal(12,2) DEFAULT NULL COMMENT '最多许提前几日',
  content varchar(255) DEFAULT NULL COMMENT '事由与所对点位的记要',
  spot_no varchar(64) DEFAULT NULL COMMENT '所对点位码（挂点位底册）',
  area_no varchar(64) DEFAULT NULL COMMENT '路段目录代号（并拨键一）',
  door_seg varchar(64) DEFAULT NULL COMMENT '门牌段（并拨键二）',
  contact varchar(128) DEFAULT NULL COMMENT '联系栏',
  status int DEFAULT NULL COMMENT '条目情形 0搁着(未启动) 1已发出(已派发) 2发不成(派不出)',
  sent_at datetime DEFAULT NULL COMMENT '派发那一刻（两边记号之一）',
  sent_post varchar(64) DEFAULT NULL COMMENT '接手的岗位（两边记号之一）',
  fail_step varchar(64) DEFAULT NULL COMMENT '卡住的那一步',
  fail_reason varchar(255) DEFAULT NULL COMMENT '派不出原因（毛病出在哪一格）',
  round_no varchar(32) DEFAULT NULL COMMENT '本轮轮次码（三个数只讲本次这一轮）',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='届期催办与巡查条目';

CREATE TABLE IF NOT EXISTS t_oads_rest_day (
  id bigint NOT NULL COMMENT '主键',
  rest_day date NOT NULL COMMENT '休办那一日',
  kind int DEFAULT NULL COMMENT '休办来路 1法定节假日 2市里下文停办',
  note varchar(255) DEFAULT NULL COMMENT '依据文号或说明',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='休办日历（法定节假日与停办日，数办工日只认这一张表）';

CREATE TABLE IF NOT EXISTS t_oads_post_quota (
  id bigint NOT NULL COMMENT '主键',
  post_no varchar(64) NOT NULL COMMENT '岗位代号',
  quarter_code varchar(8) NOT NULL COMMENT '季度码 如2026Q4',
  quota_total int DEFAULT NULL COMMENT '本季定额件数',
  status int DEFAULT '0' COMMENT '定额情形 0在用 1停用',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='岗位季度派发定额';

CREATE TABLE IF NOT EXISTS t_oads_permit_bill (
  id bigint NOT NULL COMMENT '主键',
  bill_no varchar(64) DEFAULT NULL COMMENT '设置许可核准单',
  node_no int DEFAULT NULL COMMENT '当前所在关 0..2（窗口受理/区局核准/市局备案）',
  sign_mode int DEFAULT NULL COMMENT '本关算齐办法 0一名即过 1两关须同判',
  need_count int DEFAULT NULL COMMENT '本关应签人数',
  sign_count int DEFAULT NULL COMMENT '本关已到人数',
  status int DEFAULT NULL COMMENT '核准情形 0在签 1已点齐 2已压回',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='设置许可核准单';

CREATE TABLE IF NOT EXISTS t_oads_rule_line (
  id bigint NOT NULL COMMENT '主键',
  rule_code varchar(64) DEFAULT NULL COMMENT '牌面安全归层判据线代号',
  rule_name varchar(128) DEFAULT NULL COMMENT '线名',
  obj_kind varchar(32) DEFAULT NULL COMMENT '这条线管点位册面还是核准卷面',
  th1_max decimal(12,3) DEFAULT NULL COMMENT '起算限值',
  th2_max decimal(12,3) DEFAULT NULL COMMENT '够线限值',
  th3_max decimal(12,3) DEFAULT NULL COMMENT '封顶限值',
  eff_start datetime DEFAULT NULL COMMENT '起用那一一日',
  eff_end datetime DEFAULT NULL COMMENT '交班那一一日(不含)',
  priority int DEFAULT NULL COMMENT '交班次序(数值大的先说话)',
  status int DEFAULT NULL COMMENT '线的情形 0在位 1已撤下',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='牌面安全归层判据线';

CREATE TABLE IF NOT EXISTS t_oads_spot (
  id bigint NOT NULL COMMENT '主键',
  spot_no varchar(64) DEFAULT NULL COMMENT '点位码',
  area_id int DEFAULT NULL COMMENT '挂在哪一段册名下',
  area_no varchar(64) DEFAULT NULL COMMENT '段册代号（冗余自段册）',
  set_no varchar(64) DEFAULT NULL COMMENT '设置主体代号或承办岗代号',
  need_num int DEFAULT NULL COMMENT '核定应设处数',
  got_num int DEFAULT NULL COMMENT '已核验落地处数',
  lack_num int DEFAULT NULL COMMENT '还差几处（轧出来，不手填）',
  content varchar(255) DEFAULT NULL COMMENT '随件交来的牌面题名与要件',
  status int DEFAULT NULL COMMENT '册面情形 0新入册 1已核齐 2缺项',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='点位底册';

-- 初始档案数据（t_oads_area_book 预置两条段册种子）
-- area_id=0 为在册段册（主干道类），area_id=1 为已撤出的旧段册（次干道类），供挂接校验与新入册校验取用。
-- 段册代号与类别按路段类型给值。
INSERT IGNORE INTO t_oads_area_book (id, area_no, area_name, area_kind, road_name, status, del_flag, create_by, create_time)
VALUES (0, 'DU00', '澜原省江水市云桥区临江路段（主干道类，在册）', '主干道', '澜原省—江水市—云桥区—临江街巷', 0, 0, 'seed', NOW()),
       (1, 'DU01', '南津区北岸段·并走撤出的旧段册（已撤出）', '次干道', '澜原省—江水市—南津区—北岸街巷', 1, 0, 'seed', NOW());

