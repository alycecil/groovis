
/*
 * @Author wcecil
 */

@Grape(
    @Grab(group='redis.clients', module='redis', version='2.6.2')
    
)

import groovy.grape.Grape
import groovy.json.*
import redis.clients.jedis.Jedis
import redis.clients.jedis.ScanParams
import redis.clients.jedis.ScanResult

class BasicBean {
	def a;
        def b;
        def c;
}

class JSONWrapper {
    Object obj;
    String clz;
    
    
    static def wrap(Object b){
        def _b = new JSONWrapper();
        
        _b.clz = b.getClass().name;
        _b.obj=b
        
        return _b;
    }
    static def hydrate(String json){
        def o = new JsonSlurper().parseText(json);
        
        
        def res = new JSONWrapper().getClass().classLoader.loadClass( 
            o.clz )?.newInstance()
        
        
        ((Map)o.obj).entrySet().each {
            res."$it.key" = it.value;
        }
        
        return res;
    }
}

class GroovisSetIterator<T> implements Iterator<T>{

    String key, cursor;
    private final ScanParams params;
    
    private T next;
    private static final String INITIAL_CURSOR = "0";
    
    public GroovisSetIterator(String key) {
        this.key = key;
        cursor = INITIAL_CURSOR;
        
        //only get one at a time
        params = new ScanParams();
        params.count(1);
    }

    @Override
    public boolean hasNext() {
        if(next != null){
            return true;
        }
        
        ScanResult<String> res = ConnectionManager.getJedis().sscan(key, cursor, params);
        
        cursor = res.getStringCursor();
        
        
        List<String> list = res.getResult();
        
        if(list.isEmpty() || cursor.equals(INITIAL_CURSOR)){
            return false;
        }
        
        if(list.size()>1){
            throw new IllegalStateException("JEDIS IGNORING MAX COUNT OF 1");
        }
        
        
        String top = list.get(0);
        
        next = JSONWrapper.hydrate(top);
        
        return true;
    }

    @Override
    public T next() {
        if(next == null){
            hasNext();
        }
        if(next == null){
            throw new NoSuchElementException();
        }
        T res;
        res = next;
        next = null;
        return res;
    }
    
}


class GroovisSet<T> implements Set<T>{
    String key;
    private String key;
    
    GroovisSet(String k){
        this.key = k;
    }

    public int size() {
        return ConnectionManager.getJedis().scard(key).intValue();
    }

    public boolean isEmpty() {
        return !ConnectionManager.getJedis().exists(key);
    }

    @Override
    public boolean contains(Object o) {
        def s= wrap(o);
        return ConnectionManager.getJedis().sismember(key, s);
    }

    @Override
    public Iterator<T> iterator() {
        return new GroovisSetIterator(key);
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean add(T e) {
        String s = wrap(e);
        return ConnectionManager.getJedis().sadd(key, s)>0;
    }

    @Override
    public boolean remove(Object o) {
        String s = wrap(o);
        return ConnectionManager.getJedis().sadd(key, s)>0;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        if(c==null) throw new NullPointerException();
        if(c.isEmpty())return false;
        List<String> list = new ArrayList();
        for(T t : c){
           def s = wrap(t);
           list.add(s);
        }
        return ConnectionManager.getJedis().sadd(key, (String[]) list.toArray())>0;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if(c==null) throw new NullPointerException();
        if(c.isEmpty())return false;
        List<String> list = new ArrayList();
        for(Object it : c){
           def s = wrap(it);
           list.add(s);
        }
        return ConnectionManager.getJedis().srem(key, (String[]) list.toArray())>0;
    }

    @Override
    public void clear() {
        ConnectionManager.getJedis().del(key);
    }

    
    def wrap(def o) {
        def _o = JSONWrapper.wrap(o);
        return JsonOutput.toJson(
            _o
        );
    }
    
}




class ConnectionManager {
    static Jedis j;
    
    public static open(){
        j = new Jedis("localhost");
        
    }
    
    public static close(){
        j.close();
        j=null;
    }
    
    public static Jedis getJedis(){
        if(j==null){
            open();
        }
        
        j
    }
}










//
// THIS IS WHAT YOURE LOOKING FOR
//

def myset = "abcdef"
def gs = new GroovisSet<BasicBean>(myset)

def b = new BasicBean()
b.a ="Hello"
b.b = 123


gs.add(b)


b = new BasicBean()
b.a ="Hi"
b.b = 1233
b.c = 'oops'

gs.add(b)


println "Size : "+gs.size()
println "$gs.empty"

gs.each {
    println it
    println JsonOutput.toJson(it);
}
