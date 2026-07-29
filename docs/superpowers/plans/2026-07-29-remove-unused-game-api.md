# 清理未使用游戏接口实现计划

**目标：** 删除已无生产调用的兼容接口，保持当前 Controller 直接使用游戏策略的流程不变。

**范围：**
- `QuestionService.generateRound(...)`
- `QuestionService.factory()`
- `GameStrategyFactory.get(String)`
- `GameStrategyFactory.resolveMode(String)`
- 仅测试上述接口的旧单测

**验证：** 先移除旧单测调用，再删除实现；使用 `mvn test` 进行后端全量回归。
