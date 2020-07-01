package cn.weit.common.support;

import cn.weit.common.dao.MongoBaseDao;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.query.Criteria;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Field;

import java.util.List;

/**
 * @author weitong
 */
public class MongoDaoSupport <T extends Serializable> implements MongoBaseDao<T> {
    @Autowired
    @Qualifier("mongoTemplate")
    protected MongoTemplate mongoTemplate;

    public T save(T bean) {
        mongoTemplate.save(bean);
        return bean;
    }

    public void deleteById(T t) {
        mongoTemplate.remove(t);
    }


    public void deleteByCondition(T t) {
        Query query = buildBaseQuery(t);
        mongoTemplate.remove(query, getEntityClass());
    }

    public void update(Query query, Update update) {
        mongoTemplate.updateMulti(query, update, this.getEntityClass());
    }

    public void updateById(String id, T t) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(id));
        Update update = buildBaseUpdate(t);
        update(query, update);
    }

    public List<T> find(Query query) {
        return mongoTemplate.find(query, this.getEntityClass());
    }

    public List<T> findByCondition(T t) {
        Query query = buildBaseQuery(t);
        return mongoTemplate.find(query, getEntityClass());
    }

    public T findOne(Query query) {
        return mongoTemplate.findOne(query, this.getEntityClass());
    }


    public T get(String id) {
        return mongoTemplate.findById(id, this.getEntityClass());
    }

    public T get(String id, String collectionName) {
        return mongoTemplate.findById(id, this.getEntityClass(), collectionName);
    }

    private Query buildBaseQuery(T t) {
        Query query = new Query();
        Field[] fields = t.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(t);
                if (value != null) {
                    org.springframework.data.mongodb.core.mapping.Field queryField = field.getAnnotation(org.springframework.data.mongodb.core.mapping.Field.class);
                    if (queryField != null) {
                        query.addCriteria(Criteria.where(queryField.value()).is(value));
                    }else{
                        query.addCriteria(Criteria.where(field.getName()).is(value));
                    }
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return query;
    }

    private Update buildBaseUpdate(T t) {
        Update update = new Update();

        Field[] fields = t.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(t);
                if (value != null) {
                    update.set(field.getName(), value);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return update;
    }

    @SuppressWarnings("unchecked")
    protected Class<T> getEntityClass() {
        return ((Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
    }

    public MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }
}
