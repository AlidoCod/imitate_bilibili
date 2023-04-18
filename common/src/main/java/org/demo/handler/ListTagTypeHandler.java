package org.demo.handler;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.demo.pojo.base.Tag;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * mp 自定义类型转换器
 */
@MappedJdbcTypes(value = JdbcType.VARCHAR)
@MappedTypes(value = List.class)
public class ListTagTypeHandler extends BaseTypeHandler<List<Tag>> {

    /**
     * 以,为分隔符分割枚举存入数据库中
     */
    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, List<Tag> tags, JdbcType jdbcType) throws SQLException {
        StringBuilder sb = new StringBuilder();
        tags.forEach((tag) -> {
            sb.append(tag.getValue()).append(",");
        });
        sb.deleteCharAt(sb.length() - 1);
        preparedStatement.setString(i, sb.toString());
    }

    @Override
    public List<Tag> getNullableResult(ResultSet resultSet, String s) throws SQLException {
        String result = resultSet.getString(s);
        return parseValue(result);
    }

    @Override
    public List<Tag> getNullableResult(ResultSet resultSet, int i) throws SQLException {
        String s = resultSet.getString(i);
        return parseValue(s);
    }

    @Override
    public List<Tag> getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        String s = callableStatement.getString(i);
        return parseValue(s);
    }

    private List<Tag> parseValue(String value) {
        if (Strings.isNullOrEmpty(value))
            return new ArrayList<>();
        Iterable<String> values = Splitter.on(',').split(value);
        List<Tag> list = new ArrayList<>();
        values.forEach((i) -> {
            list.add(Tag.getTagByValue(Integer.parseInt(i)));
        });
        return list;
    }
}
