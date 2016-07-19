# NFCSample
该工程是NFC（近场通讯功能）读取rfid卡片例子
也可使用NFC读取tag标签
NFC有很多tech，每个都有相对应的tech列表和规范
识别结果有打印在log没有在界面上显示

PS：数据格式说明
给予MifareClassic的射频卡，一般内存大小有3种：

1K: 16个分区(sector)，每个分区4个块(block)，每个块(block) 16个byte数据

2K: 32个分区，每个分区4个块(block)，每个块(block) 16个byte数据

4K：64个分区，每个分区4个块(block)，每个块(block) 16个byte数据
