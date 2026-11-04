# 本项目的 common / target 划分

主版本为 `targets/forge-1.20.1`，其余 target 从它移植。本文只记录**已通过源码/发布物确证**的差异，不作为未验证版本的兼容性声明。

## 依赖可用性

| 依赖 | forge 1.20.1 | neoforge 1.21.1 | 其他 target |
| --- | --- | --- | --- |
| ApricityUI | `com.sighs:ApricityUI-forge-1.20.1:1.2.9` | `com.sighs:ApricityUI-neoforge-1.21.1:1.2.9.5` | 模板缓存中存在 1.18.2 / 1.19.2 / fabric / 26.1 发布物 |
| SimpleBedrockModel | `2.5.7-forge-mc1.20.1` | `2.5.7-neoforge-mc1.21.1` | **无发布物** |
| MAE | `1.1.2` | `1.1.2` | 纯 Java 库 |

SimpleBedrockModel 的 JitPack `maven-metadata.xml` 只列出 `*-forge-mc1.20.1` 与 `*-neoforge-mc1.21.1` 版本，Modrinth 发布页也只声明 Forge/NeoForge。装饰方块、装饰物品和预览界面的核心渲染依赖该库，因此在 Fabric、1.18.2、1.19.2、26.1 上无法保持功能对等。对应 target 保持模板状态，未移植渲染功能，也没有用空实现掩盖缺失。

## SPI

`common` 中的 `org.ywzj.decoration.platform` 只依赖 Minecraft 类型：

- `DecorationPlatform`：客户端判定、游戏目录、模组版本、打开菜单、三种网络发送方向。
- `DecorationClientPlatform`：打开装饰设置界面、读取展示描述、注册资源重载监听、重载客户端资源、重建创造模式页签。
- `DecorationPlatforms`：唯一的安装/读取点，由 target 在模组构造阶段安装。

`common` 中的 `org.ywzj.decoration.registry` 是注册结果的中立句柄：

- `DecorationRegistry`：方块、方块实体类型、物品、菜单类型、配方类型/序列化器的句柄。
- `DecorationItems`：按展示 ID 动态注册的装饰物品映射。

target 负责 `DeferredRegister` / 注册事件 / 网络实现 / 资源包来源 / 客户端注册；common 只读取句柄，不出现任何 loader 导入。

## 已确证并下沉到 target 的内容

| common 路径 | 原因 | target 实现 |
| --- | --- | --- |
| `all/*` | 注册 API、事件总线、网络通道均为 loader 专属 | forge / neoforge 各自的 `all` 包 |
| `network/Channel.java` | Forge `SimpleChannel` 与 NeoForge `PayloadRegistrar` 不兼容 | 各 target 的 `Channel` |
| `custom/CommonAssetsManager.java` | `AddReloadListenerEvent` / `OnDatapackSyncEvent` 的包名与发送 API 不同 | 各 target 的事件类 |
| `resource/DecorationPackLoader.java` | Forge `PathPackResources` / `DelegatingPackResources` 在 NeoForge 1.21 已移除 | `ForgeDecorationPackSource`、`NeoForgeDecorationPackSource` |
| `command/*` | 重载命令需要 loader 的客户端分发与客户端资源管理器 | 各 target 的命令实现 |
| `client/**` | GUI、渲染、资源重载事件与 SimpleBedrockModel 绑定 | 各 target 的 client 包 |
| `recipe/SewingRecipeSerializer.java` | 1.20.1 使用 Gson + `CraftingHelper`；1.21.1 使用 `MapCodec` + `StreamCodec` | 各 target 的序列化器 |
| `item/PlainTextureItem`、`blockentity/*`、`recipe/SewingRecipe`、`network/message/ClientSewingAction` | 1.20.1 → 1.21.1 的 NBT/DataComponent、`saveAdditional`/`loadAdditional`、`RecipeInput`/`RecipeHolder` 变化 | neoforge target 通过 `commonJava { exclude ... }` 排除并提供实现 |

## 1.20.1 → 1.21.1 的源码证据

框架自带迁移参考中的对应条目：

- `docs/version-differences/01-1.20.4-to-1.20.5/03.md`：`ItemStack` 的 `CompoundTag` 改为 Data Components；`getOrCreateTag` / `setTag` 等被移除。
- `docs/version-differences/01-1.20.4-to-1.20.5/16.md`：`BlockEntity#load` → `loadAdditional`，`saveAdditional` / `getUpdateTag` 增加 `HolderLookup.Provider`。
- `docs/version-differences/03-1.20.6-to-1.21/03.md`：`ResourceLocation` 构造器私有化，改用 `fromNamespaceAndPath` / `parse`。
- `docs/version-differences/03-1.20.6-to-1.21/09.md`：`Recipe` 改为 `RecipeInput`，`RecipeManager` 返回 `RecipeHolder`。

因此 `neoforge-1.21.1` 的 `commonJava` 排除项只包含上述已确证文件，其他 common 文件由两个 target 共同编译。

## 验证状态

- `common`：`gradlew -p common build`（Forge 1.20.1 类路径）通过。
- `forge-1.20.1`：`gradlew build` 通过，jar 内含共享类、共享资源、metadata 与 jarJar 依赖。
- `neoforge-1.21.1`：编译与打包状态见根目录 README 的支持矩阵；未做运行期行为验证。
- 其余 target：未移植，原因见“依赖可用性”。
