# groovis
Redis collection wrapper, written in groovy.

# description
this is a goofy little proof of concept of saving a object to a redis set using jedis and a json string; reading said string back and seeing it again as an object with an iterator

its about as simple a redis proof of concept you can do but its a basis of a redis stack

you really should be using redisson instead of this, if you want abstracted redis-backed collections. Its a great library and through this great community its only getting better.

# author
Written by william cecil


# licence 
All code here within is in the public domain, offered without warenty, limit, or expectation from any party.

# dependencies
I didnt have maven ... or an ide while writing this so forgive me for not including a pom; its graped so enjoy.
(netbeans is a text editor for groovy, oh how the mighty have fallen)

Uses Jedis 2.6.2

