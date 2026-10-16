# 世界数据包说明核对

- 用户约定：docs 仅存放面向用户的使用文档；规划、调查和执行记录存放在 .planning 下的对应子目录。
- ClientDatapackEvents 仅处理客户端退出连接，清理远端规则快照；不负责加载本地数据包。
- CoinLootEntry.expand 委托原版判断条件，并包装候选条目的 getWeight，在抽取时读取已发布的数据包配置。
- WorldDatapackBootstrap.ensure 首次初始化时从 COMMON Config 导出世界包，已有同名目录/zip 保留，世界标记阻止再次导出。
- 配置文件路径：<world>/datapacks/isaac_disaster/data/isaac_disaster/gameplay/settings.json。
- prepare 阶段的解析异常通过原版异步 reload 传播；首次生成、ServerAboutToStart 的同步补读和编程错误断言需与此区分，不能声称所有异常都会进入原版 GUI。

## 原版源码核对结论

- 已核对本机 Forge 1.20.1-47.4.9 源码包。ReloadCommand.reloadPacks 对 reloadResources 的异常 Future 发出 commands.reload.failure，日志保留异常详情。
- 已有世界的 WorldOpenFlows.doLoadLevel 在 loadWorldStem 失败时显示 DatapackLoadFailureScreen；创建世界界面验证失败也有单独的 ConfirmScreen。
- createPackRepository 调用位于这些 try/catch 之外，因此 Bootstrap.ensure 生成文件时的异常不能保证进入数据包失败界面。
- ServerAboutToStartEvent 在 IntegratedServer.initServer 内调用；若此时同步补读配置抛出异常，会进入 MinecraftServer.runServer 的崩溃处理，IntegratedServer.onServerCrash 进一步通知客户端崩溃。
- 原版 SimpleJsonResourceReloadListener 也存在记录单文件解析错误后跳过的策略，并非每种数据包错误都拒绝整次加载。
- 新生成的是世界文件夹包 file/isaac_disaster，和 Forge 自动加载的模组资源包不是同一个；加载器按固定资源位置读取，任意启用包均可提供对应 JSON。
- 当前工作区未发现名为 isaacDisaster 的外部包，不能确认用户提到的具体包身份。

## 2026-10-16 第一轮实现（失败处理已按下节收敛）

- 用户已批准后续方案，采用并行独立任务实现钱币/客户端清理与道具定义，主代理整合加载时机、初始化、兼容和验证。
- ClientForgeEvents.onPlayerLoggedOut 清理服务器玩法数据的客户端缓存；IsaacClientConfig 完全独立，无网络写入。
- CoinLootEntry 继承原版 expand/weight/quality/luck；随机表显式 93/6/1；serializer 调用父类保留公共字段。
- 移除三项权重和两项道具 Config/Catalog/GameplayConfig 字段，现行模板 64 键；旧 JSON 中这五键警告后忽略，其余未知键仍报错。
- HolyShieldSettings 默认 strength_per_stack=3.0，MoneyIsPowerSettings 默认 damage_per_coin=0.007；均为固定默认值，范围 [0,99999]，不读取 TOML。读写、生成、网络共享注册清单。
- Bootstrap 全部标识从 MOD_ID 派生。generated.json 清单用于补充新登记文档，保留已有文档且不恢复已记录后被删除的文件；预装 ZIP 不改写。
- Bootstrap 仓库 RETURN 阶段生成文件并记录可恢复错误，PackConfig.createResourceManager HEAD 将初始化错误转入原版失败处理；RETURN 校验实际世界资源，失败关闭新管理器。
- ServerAboutToStart 只安装已验证快照，不同步解析 JSON。客户端新建世界两条路径通过 WrapMethod 补充失败提示；已有世界仍走原版安全模式界面。
- ReloadCommand 的 WrapOperation 为自定义数据失败补充具体文件/来源/原因。整体 reload 成功才安装 Snapshot，失败保留旧配置和道具数据。
- 网络玩法快照扩展为所有登记文档，协议提升为 2 并严格匹配；不更改本地客户端配置或本地新世界模板。

## 2026-10-16 按用户要求复用原版失败路径

- 用户明确要求移除为处理失败新增的全部三个 Mixin；接受加载阶段直接报错，不为错误提示额外介入原版。
- 删除 WorldDatapackValidationMixin、WorldDatapackReloadMixin、WorldDatapackClientMixin 及配置登记。
- 删除 WorldDatapackLoading 的延迟错误/提前校验桥接，以及专用 DatapackLoadException；保留普通异常中的文件和来源包上下文。
- BootstrapMixin 直接调用初始化函数，初始化失败直接传播，不缓存、重定向错误。
- AddReloadListenerEvent 完成解析和暂存，OnDatapackSyncEvent 在完整重载成功后发布；新建世界资源管理器没有准备结果时在初始化阶段直接读取实际资源。
- /reload 的失败提示完全由原版 ReloadCommand 产生，详细原因仍由原版异常日志记录；失败不发布新快照。
- 当前数据包相关 Mixin 仅保留 Bootstrap 与 Order 两个，分别负责生成与首次优先级。

## 提交前清理后的当前状态

- 用户确认当前没有旧版本，已删除五个旧字段白名单、仅部分 GameplaySettings 的包构造与客户端安装重载、settings() 别名，以及补文档升级清单。
- 所有同步入口仅接收完整 Snapshot，缺文档网络载荷直接报错；正常数据包文件/字段缺省语义仍按当前格式保留。
- Bootstrap 只在初始化标记不存在时生成全套文件，无升级补文件路径，也不读写 generated.json。
- 神圣屏障默认值为用户最新指定的1.0；先前3.0仅为历史记录。
- OCR 在当前 PATH 未找到，使用已获授权的人工审查回退；审查快照位于 build/review/precommit，覆盖当前暂存的65个文件。

### 提交审查确认问题

- P2：GameplaySettingsS2CPacket.toBytes 仅在发送时限制完整 JSON 为131072字符，GameplaySettings 允许更长的 magicNumberIntroduction。
- 已用忽略目录内的临时 JUnit 验证：长度131073的字符串可通过 GameplaySettings.parse，但对完整 Snapshot 调用 packet.toBytes 必然抛 EncoderException。
- IsaacDatapackEvents.sync 在发送前已 install 新快照并刷新玩家，在线重载因此可能报失败但服务器已换规则，客户端仍保留旧数据；无玩家时问题延迟到登录。
- Forge47.4.9发送编码链为同步调用。建议在资源准备阶段验证完整快照可同步长度或调整编码，确保失败发生在安装前。本轮仅报告此发现，未扩展修改该行为。

### 同步长度保护已实现

- `IsaacDatapackEvents` 的准备阶段和实际资源回退读取统一调用 `loadForSync`；它使用 `GameplaySettingsS2CPacket.toBytes` 对完整快照做一次预检，再释放临时缓冲区。
- 超过 `writeUtf` 上限时，错误发生在快照安装前；原版 reload 失败流程保留旧服务器/客户端数据。
- 未增加 Mixin、协议版本或兼容入口。
