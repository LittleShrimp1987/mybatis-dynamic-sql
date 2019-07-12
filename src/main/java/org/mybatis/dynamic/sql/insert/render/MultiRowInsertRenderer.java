/**
 *    Copyright 2016-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.dynamic.sql.insert.render;

import static org.mybatis.dynamic.sql.util.StringUtilities.spaceBefore;

import java.util.Objects;
import java.util.function.Function;

import org.mybatis.dynamic.sql.insert.MultiRowInsertModel;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.util.InsertMapping;

public class MultiRowInsertRenderer<T> {

    private MultiRowInsertModel<T> model;
    private RenderingStrategy renderingStrategy;
    
    private MultiRowInsertRenderer(Builder<T> builder) {
        model = Objects.requireNonNull(builder.model);
        renderingStrategy = Objects.requireNonNull(builder.renderingStrategy);
    }
    
    public MultiRowInsertStatementProvider<T> render() {
        MultiRowInsertValuePhraseVisitor visitor =
                new MultiRowInsertValuePhraseVisitor(renderingStrategy, model.recordCount());
        FieldAndMultipleValuesCollector collector = model.mapColumnMappings(toFieldAndMultipleValues(visitor))
                .collect(FieldAndMultipleValuesCollector.collect());
        
        return new DefaultMultiRowInsertStatementProvider.Builder<T>().withRecords(model.records())
                .withInsertStatement(calculateInsertStatement(collector))
                .build();
    }
    
    private Function<InsertMapping, FieldAndMultipleValues> toFieldAndMultipleValues(
            MultiRowInsertValuePhraseVisitor visitor) {
        return insertMapping -> toFieldAndValue(visitor, insertMapping);
    }
    
    private FieldAndMultipleValues toFieldAndValue(MultiRowInsertValuePhraseVisitor visitor,
            InsertMapping insertMapping) {
        return insertMapping.accept(visitor);
    }

    private String calculateInsertStatement(FieldAndMultipleValuesCollector collector) {
        return "insert into" //$NON-NLS-1$
                + spaceBefore(model.table().tableNameAtRuntime())
                + spaceBefore(collector.columnsPhrase())
                + spaceBefore(collector.valuesPhrase());
    }
    
    public static <T> Builder<T> withMultiRowInsertModel(MultiRowInsertModel<T> model) {
        return new Builder<T>().withMultiRowInsertModel(model);
    }
    
    public static class Builder<T> {
        private MultiRowInsertModel<T> model;
        private RenderingStrategy renderingStrategy;
        
        public Builder<T> withMultiRowInsertModel(MultiRowInsertModel<T> model) {
            this.model = model;
            return this;
        }
        
        public Builder<T> withRenderingStrategy(RenderingStrategy renderingStrategy) {
            this.renderingStrategy = renderingStrategy;
            return this;
        }
        
        public MultiRowInsertRenderer<T> build() {
            return new MultiRowInsertRenderer<>(this);
        }
    }
}
