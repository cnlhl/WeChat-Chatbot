# WeChat-Chatbot

> Based on WeChat 8.0.20, Android version

An automatic WeChat Chatbot, implemented in Xposed framwork and Java, integrating with Qingyunke chatbot API.

## Dynamic& Static Analysis

- 通过Android monitor反复检查微信收到消息时所调用的方法，经过筛选和网上的方法比较，初步确定为SQLiteDatabase.insert()方法
- 根据查阅到的信息，这个函数用于往客户端本地数据库插入数据
- hook该函数，打印其参数来验证猜想
- 可以观察到第一个参数表明该数据的类型：message/ appmessage/ msgquote/ WeChatsessionhistory
- 而第二个参数为进一步的附加信息：msgID/rowID等，标注信息来源
- 通过反复接收信息测试，判断message类为接受的新信息标识，因此只需要判断该函数的调用中参数1的值是否为“message”即可知道是否需要自动回复；
- 而回复消息的函数，在task2中其实已经找到并hook验证过：
- 由于aKl函数并不是static型方法，还需要找到可以创建实例的aKl引用
- 利用jadx查找aKl的用例：转到ChatFooter类中
- 找到对应aKl调用的接口：MgP；只要找到实例化MgP的对象即可
- 找到ChatFooter类下的setFooterEventListener方法完成了对MgP的实例化，它是最终的hook对象；
- 但是这样设计出来的机器人有一个很大的缺陷：无法读取对方发送的信息内容，并且只能在当前聊天窗口前发送信息。于是继续查阅相关博客链接。尝试使用新的方法。
- 经过筛选，确定handlemessage函数，其位于notification下的一个匿名类中，从函数内容也可以看到它提供了我们针对性回复所需要的信息：聊天对象和信息内容
- 接下来进一步寻找回复消息函数aKl的内部逻辑，来实现非当前窗口自动回复
- 参数str被传入到buA函数
- buA函数又继续将其传入到hv函数，
- 在hv函数中，通过和参考链接博客的内容比较，判断函数a为最终的对象，
- 由于a函数并非static方法，还需要找到能够实例化他的类。
- 在a的类class j中找到接口a
- 搜索该接口的交叉引用，定位到kernal包中的h class下
- 可以看到h完成了实例化
- 初始化h的对象Hhy
- hHy在函数aGF中被实例化
- 而class h下的hHG完成了对接口a的调用，从而最终实例化了我们的目标
- 至此，调用a的方法也出来了：调用kernel包下的aGF函数，通过它来调用hHG，从而实例化class t，实现间接调用函数a。


## Xposed Hook
refer to the code [hook_wechat_robot.java](./hook_wechat_robot.java)

- Hook接收消息的函数handlemessage，当该函数被调用的时候进行自动回复
- 从handlemessage的参数中获取相应的信息
- 找到需要调用的回复函数a
- 拼接回复函数需要的参数，并从服务器根据发送的消息请求回复内容
- 获取自动回复内容的函数，使用了青云客的API
