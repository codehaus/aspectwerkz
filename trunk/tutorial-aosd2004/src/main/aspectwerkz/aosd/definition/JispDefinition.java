/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD-style license *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.definition;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Holds the definition of the persistence concern.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public final class JispDefinition implements Definition {

    public String getName() {
        return _name;
    }

    public void setName(final String name) {
        _name = name;
    }

    public String getDbPath() {
        return _dbPath;
    }

    public void setDbPath(final String path) {
        _dbPath = path;
    }

    public void setCreateDbOnStartup(final boolean flag) {
        _createDbOnStartup = flag;
    }

    public boolean getCreateDbOnStartup() {
        return _createDbOnStartup;
    }

    public Collection getBtreeIndexes() {
        return _btreeIndexes;
    }

    public Collection getHashIndexes() {
        return _hashIndexes;
    }

    public void addBtreeIndex(final BTreeIndexDefinition btreeIndexDefintion) {
        _btreeIndexes.add(btreeIndexDefintion);
    }

    public void addHashIndex(final HashIndexDefinition hashIndexDefintion) {
        _hashIndexes.add(hashIndexDefintion);
    }

    public Collection getPersistentObjectDefinitions() {
        return _persistentObjectDefinitions;
    }

    public void addPersistentObjectDefinition(final PersistentObjectDefinition persistentObjectDefinition) {
        _persistentObjectDefinitions.add(persistentObjectDefinition);
    }

    private String _name = null;
    private String _dbPath = null;
    private boolean _createDbOnStartup = false;
    private Collection _btreeIndexes = new ArrayList();
    private Collection _hashIndexes = new ArrayList();
    private Collection _persistentObjectDefinitions = new ArrayList();

    public final static class BTreeIndexDefinition implements Definition {

        public String getName() {
            return _name;
        }

        public void setName(final String name) {
            _name = name;
        }

        public String getKeyType() {
            return _keyType;
        }

        public void setKeyType(final String keyType) {
            _keyType = keyType;
        }

        public int getOrder() {
            return _order;
        }

        public void setOrder(final int order) {
            _order = order;
        }

        private String _name = null;
        private String _keyType = null;
        private int _order = 23;
    }

    public final static class HashIndexDefinition implements Definition {

        public String getName() {
            return _name;
        }

        public void setName(final String name) {
            _name = name;
        }

        public String getKeyType() {
            return _keyType;
        }

        public void setKeyType(final String keyType) {
            _keyType = keyType;
        }

        public int getBuckets() {
            return _buckets;
        }

        public void setBuckets(final int buckets) {
            _buckets = buckets;
        }

        public int getDbSize() {
            return _dbSize;
        }

        public void setDbSize(final int dbSize) {
            _dbSize = dbSize;
        }

        private String _name = null;
        private String _keyType = null;
        private int _buckets;
        private int _dbSize;
    }

    public final static class PersistentObjectDefinition implements Definition {

        public String getClassname() {
            return _class;
        }

        public void setClassname(final String klass) {
            _class = klass;
        }

        public Collection getIndexes() {
            return _indexes;
        }

        public void addIndex(final Index index) {
            _indexes.add(index);
        }

        private String _class = null;
        private Collection _indexes = new ArrayList();

        public final static class Index implements Definition {

            public String getName() {
                return _name;
            }

            public void setName(final String name) {
                _name = name;
            }

            public String getKeyMethod() {
                return _keyMethod;
            }

            public void setKeyMethod(final String keyMethod) {
                _keyMethod = keyMethod;
            }

            private String _name = null;
            private String _keyMethod = null;
        }
    }
}