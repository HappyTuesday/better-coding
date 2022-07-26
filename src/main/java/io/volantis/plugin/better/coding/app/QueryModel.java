package io.volantis.plugin.better.coding.app;

import com.intellij.openapi.util.text.StringUtil;
import io.volantis.plugin.better.coding.app.fetcher.FindByFiltersMethod;
import io.volantis.plugin.better.coding.app.fetcher.OrderByClause;
import io.volantis.plugin.better.coding.proxy.*;

import java.util.*;

public class QueryModel {
    public final InfoProxy target;
    public final DtoSource from;
    public final List<Join> joins;

    public QueryModel(InfoProxy target, DtoSource from, List<Join> joins) {
        this.target = target;
        this.from = from;
        this.joins = joins;
    }

    private Join joinOf(Source source) {
        for (Join join : joins) {
            if (join.source == source) {
                return join;
            }
        }
        throw new IllegalArgumentException("invalid source");
    }

    public boolean joinIsSingular(Source source) {
        if (source == from) {
            return true;
        }
        for (Source s = source; s != null; s = joinOf(s).referredSource) {
            if (s instanceof InfoSource && !((InfoSource) s).isSingular()) {
                return false;
            }
        }
        return true;
    }

    public List<JoinSpan> computeJoinPath(Join targetJoin) {
        LinkedList<JoinSpan> path = new LinkedList<>();
        for (Join join = targetJoin; ; join = joinOf(join.referredSource)) {
            JoinSpan span = new JoinSpan(join.referredSource, join.referredField, join.source, join.localField);
            path.addFirst(span);
            if (join.referredSource == from) {
                break;
            }
        }
        return path;
    }

    public boolean isSourceJoined(Source source) {
        for (Join join : joins) {
            if (join.referredSource == source) {
                return true;
            }
        }
        return false;
    }

    public static class JoinSpan {
        public final Source prevSource;
        public final DtoField prevField;
        public final Source nextSource;
        public final DtoField nextField;

        public JoinSpan(Source prevSource, DtoField prevField, Source nextSource, DtoField nextField) {
            this.prevSource = prevSource;
            this.prevField = prevField;
            this.nextSource = nextSource;
            this.nextField = nextField;
        }

        public String getPrevFieldVar() {
            return prevSource.alias + StringUtil.capitalize(prevField.getName());
        }

        public String getPrevFieldsVar() {
            return StringUtil.pluralize(getPrevFieldVar());
        }

        public String getNextFieldVar() {
            return nextSource.alias + StringUtil.capitalize(nextField.getName());
        }

        public String getNextFieldsVar() {
            return StringUtil.pluralize(getNextFieldVar());
        }
    }

    public abstract static class Source {
        public final DtoProxy dtoProxy;
        public final String alias;
        public final OrderByClause orderByClause;

        public Source(DtoProxy dtoProxy, String alias, OrderByClause orderByClause) {
            this.dtoProxy = dtoProxy;
            this.alias = alias;
            this.orderByClause = orderByClause;
        }

        public DtoSource getDtoSource() {
            if (this instanceof DtoSource) {
                return (DtoSource) this;
            } else {
                return null;
            }
        }

        public InfoSource getInfoSource() {
            if (this instanceof InfoSource) {
                return (InfoSource) this;
            } else {
                return null;
            }
        }

        public String getDtoVar() {
            return alias + "DTO";
        }

        public String getDtosVar() {
            return alias + "DTOs";
        }

        public String getDtoMapVar() {
            return alias + "DTOMap";
        }
    }

    public static class DtoSource extends Source {
        public DtoSource(DtoProxy dtoProxy, String alias, OrderByClause orderByClause) {
            super(dtoProxy, alias, orderByClause);
        }

        public String getCopierFieldName() {
            return ConverterProxy.getCopierFieldName(dtoProxy);
        }

        public String getCopyToInfoMethodName() {
            return ConverterProxy.getCopyToInfoMethodName(dtoProxy);
        }
    }

    public static class InfoSource extends Source {
        public final InfoField targetField;
        public final InfoProxy infoProxy;
        public final IntegrationMode integrationMode;

        public InfoSource(DtoProxy dtoProxy, String alias, OrderByClause orderByClause, InfoField targetField, InfoProxy infoProxy, IntegrationMode integrationMode) {
            super(dtoProxy, alias, orderByClause);
            this.infoProxy = infoProxy;
            this.targetField = targetField;
            this.integrationMode = integrationMode;
        }

        public boolean isSingular() {
            return integrationMode instanceof IntegratedAsSingular;
        }

        public String getConverterFieldName() {
            return ConverterProxy.getConverterFieldName(dtoProxy);
        }

        public String getConverterObject(DtoSource fromSource) {
            return dtoProxy.equals(fromSource.dtoProxy) ? "this" : getConverterFieldName();
        }

        public String getConvertToInfoMethodName() {
            return ConverterProxy.getConvertToInfoMethodName(infoProxy, dtoProxy);
        }

        public String getBatchConvertToInfoMethodName() {
            return ConverterProxy.getBatchConvertToInfoMethodName(infoProxy, dtoProxy);
        }

        public String getInfoVar() {
            String name = targetField.getName();
            name = StringUtil.unpluralize(name);
            if (name == null) {
                return infoProxy.getVarName();
            }
            name = StringUtil.trimEnd(name, "Map");
            name = StringUtil.trimEnd(name, "List");
            name = StringUtil.trimEnd(name, "Info");
            if (name.isEmpty()) {
                return infoProxy.getVarName();
            }
            return name + infoProxy.getClassName();
        }

        public String getInfosVar() {
            return StringUtil.pluralize(getInfoVar());
        }

        public String getInfoMapVar() {
            return getInfoVar() + "Map";
        }
    }

    public static class Join {
        public final Source referredSource;
        public final DtoField referredField;
        public final Source source;
        public final DtoField localField;

        public Join(Source referredSource, DtoField referredField, Source source, DtoField localField) {
            this.referredSource = referredSource;
            this.referredField = referredField;
            this.source = source;
            this.localField = localField;
        }

        public String getFetcherFieldName() {
            return ConverterProxy.getFetcherFieldName(source.dtoProxy);
        }

        public String getFetchMethodName() {
            List<FieldFilter> fieldFilters = getFieldFilters(false);
            return FindByFiltersMethod.computeMethodName(source.orderByClause, fieldFilters);
        }

        public String getFetchByListMethodName() {
            List<FieldFilter> fieldFilters = getFieldFilters(true);
            return FindByFiltersMethod.computeMethodName(source.orderByClause, fieldFilters);
        }

        public List<FieldFilter> getFieldFilters(boolean filterInList) {
            List<FieldFilter> fieldFilters = new ArrayList<>();
            FieldFilterMode filterMode = filterInList ? FieldFilterMode.IN_LIST : FieldFilterMode.EQUAL_TO;
            FieldFilter fieldFilter = new FieldFilter(localField.getName(), filterMode);
            fieldFilters.add(fieldFilter);
            return fieldFilters;
        }
    }

    public interface IntegrationMode {
        default IntegratedAsSingular getSingular() {
            if (this instanceof IntegratedAsSingular) {
                return (IntegratedAsSingular) this;
            } else {
                return null;
            }
        }

        default IntegratedAsList getList() {
            if (this instanceof IntegratedAsList) {
                return (IntegratedAsList) this;
            } else {
                return null;
            }
        }

        default IntegratedAsMap getMap() {
            if (this instanceof IntegratedAsMap) {
                return (IntegratedAsMap) this;
            } else {
                return null;
            }
        }
    }

    public static class IntegratedAsSingular implements IntegrationMode {
    }

    public static class IntegratedAsList implements IntegrationMode {
    }

    public static class IntegratedAsMap implements IntegrationMode {
        public final String keyField;
        public IntegratedAsMap(String keyField) {
            this.keyField = keyField;
        }
    }
}
