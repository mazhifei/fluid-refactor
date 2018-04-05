### Candidate Chinese Name:
* 
 马志飞
- - - 
### Please write down some feedback about the question(could be in Chinese):
* 
思路是将jms的操作从具体的jms实现分离开
比如你要有其他jms的实现，只要再写两个类实现客户端和服务端的两个接口，然后再将两个实现类再调用层的support中改一下，就可以实现了
如果使用spring框架的话，可以单独写一个jms相关module，暴漏不同类型的jms实现的spring配置文件给上层，然后调用层引用这个依赖，想用那种实现只要引用相关实现类的spring的配置文件就可以了
- - -
