/*
 * Copyright (C) 2014 Copyright (C) Tieto Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.bluetooth.pbap;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * BluetoothPbapAppParams define and construct the App params used in PBAP
 * communication according to PBAP profile specification v1.2.
 */
public class BluetoothPbapAppParams {
    private static final String TAG = "BluetoothPbapAppParams";

    public static final int INVALID_VALUE_PARAMETER = -1;

    // Tag IDs
    private static final int ORDER = 0x01; // 0x00= indexed, 0x01= alphanumeric,
                                           // 0x02=phonetic
    private static final int SEARCH_VALUE = 0x02;
    private static final int SEARCH_PROPERTY = 0x03; // 0x00=Name,0x01=Number,0x02=Sound
    private static final int MAX_LIST_COUNT = 0x04; // , 0x0000, 0xFFFF),
    private static final int LIST_START_OFFSET = 0x05;
    private static final int START_OFFSET_LEN = 0x02; // , 0x0000, 0xFFFF),
    private static final int PROPERTY_SELECTOR = 0x06; // , 64 bits mask
    private static final int FORMAT = 0x07; // , 0x00 = 2.1, 0x01= 3.0

    private static final int PHONEBOOK_SIZE = 0x08; // , 0x0000, 0xFFFF)
    private static final int NEW_MISSED_CALLS = 0x09; // , 0x00, 0xFF)
    private static final int PRIMARY_VERSION_COUNTER = 0x0A; // 0 to (2^128-1)
    private static final int SECONDARY_VERSION_COUNTRY = 0x0B; // 0 to (2^128-1)
    private static final int VCARD_SELECTOR = 0x0C; // ,64 bits mask
    private static final int DATABASE_IDENTIFIER = 0x0D; // 0 to (2^128-1)
    private static final int VCARD_SELECTOR_OPERATOR = 0x0E; // , 0x00 = OR,0x01
                                                             // = AND
    private static final int RESET_NEW_MISSED_CALLS = 0x0F; // 0x01=Reset

    private static final int BYTE_LEN_1 = 0x01;
    private static final int BYTE_LEN_2 = 0x02;
    private static final int BYTE_LEN_4 = 0x04;
    private static final int BYTE_LEN_8 = 0x08;
    private static final int BYTE_LEN_16 = 0x10;

    // bit 0= Download, bit 1=Browsing, bit 2= Database Identifier, bit 3=Folder
    // Version Counters,
    // bit 4= vCard Selecting, bit 5= Enhanced Missed Calls, bit 6 = X-BT-UCI
    // vCard Property
    // bit 7= X-BT-UID vCard Property, bit 8 = Contact Referencing, bit 9 =
    // Default Contact Image Format
    // bit 10~31 Reserved
    private static final int PBAP_SUPPORTED_FEATURES = 0x10;

    private int order = INVALID_VALUE_PARAMETER;
    private String searchValue = null;
    private int searchProperty = INVALID_VALUE_PARAMETER;
    private int maxListCount = INVALID_VALUE_PARAMETER;
    private int listStartOffset = INVALID_VALUE_PARAMETER;
    private long propertySelector = INVALID_VALUE_PARAMETER;
    private int format = INVALID_VALUE_PARAMETER;
    private int phonebookSize = INVALID_VALUE_PARAMETER;
    private int newMissedCalls = INVALID_VALUE_PARAMETER;
    private double primaryVersionCounter = INVALID_VALUE_PARAMETER;
    private double secondaryVersionCounter = INVALID_VALUE_PARAMETER;
    private long vCardSelector = INVALID_VALUE_PARAMETER;
    private double databaseIdentifier = INVALID_VALUE_PARAMETER;
    private int vCardSelectorOperator = INVALID_VALUE_PARAMETER;
    private int resetNewMissedCalls = INVALID_VALUE_PARAMETER;
    private long pbapSupportedFeatures = INVALID_VALUE_PARAMETER;

    public int getOrder() {
        return order;
    }

    public void setOrder(int orderValue) {
        this.order = orderValue;
    }

    public String getSearchValue() {
        return searchValue;
    }

    public void setSearchValue(String searchText) {
        this.searchValue = searchText;
    }

    public int getSearchProperty() {
        return searchProperty;
    }

    public void setSearchProperty(int searchProperty) {
        this.searchProperty = searchProperty;
    }

    public long getPropertySelector() {
        return propertySelector;
    }

    public void setPropertySelector(long propertySelector) {
        this.propertySelector = propertySelector;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public int getPhonebookSize() {
        return phonebookSize;
    }

    public void setPhonebookSize(int size) {
        this.phonebookSize = size;
    }

    public int getNewMissedCalls() {
        return newMissedCalls;
    }

    public void setNewMissedCalls(int calls) {
        this.newMissedCalls = calls;
    }

    public double getPrimaryVersionCounter() {
        return primaryVersionCounter;
    }

    public void setPrimaryVersionCounter(double versionCounter) {
        this.primaryVersionCounter = versionCounter;
    }

    public double getSecondaryVersionCounter() {
        return secondaryVersionCounter;
    }

    public void setSecondaryVersionCounter(double versionCounter) {
        this.secondaryVersionCounter = versionCounter;
    }

    public int getMaxListCount() {
        return maxListCount;
    }

    public void setMaxListCount(int maxListCount) throws IllegalArgumentException {
        if (maxListCount < 0 || maxListCount > 0xFFFF)
            throw new IllegalArgumentException("Out of range, valid range is 0x0000 to 0xFFFF");
        this.maxListCount = maxListCount;
    }

    public int getListStartOffset() {
        return listStartOffset;
    }

    public void setListStartOffset(int startOffset) {
        this.listStartOffset = startOffset;
    }

    public long getvCardSelector() {
        return vCardSelector;
    }

    public void setvCardSelector(long vCardSelector) {
        this.vCardSelector = vCardSelector;
    }

    public int getvCardSelectorOperator() {
        return vCardSelectorOperator;
    }

    public double getDatabaseIdentifier() {
        return databaseIdentifier;
    }

    public void setDatabaseIdentifier(double dbIdentifier) {
        this.databaseIdentifier = dbIdentifier;
    }

    public void setvCardSelectorOperator(int vCardSelectorOperator) {
        this.vCardSelectorOperator = vCardSelectorOperator;
    }

    public int getResetNewMissedCalls() {
        return resetNewMissedCalls;
    }

    public void setResetNewMissedCalls(int resetMissedCalls) {
        this.resetNewMissedCalls = resetMissedCalls;
    }

    public long getPbapSupportedFeatures() {
        return pbapSupportedFeatures;
    }

    public void setPbapSupportedFeatures(long SupportedFeatures) {
        this.pbapSupportedFeatures = SupportedFeatures;
    }

    /**
     * Get the approximate length needed to store the appParameters in a byte
     * array.
     * 
     * @return the length in bytes
     * @throws UnsupportedEncodingException
     *             if the platform does not support UTF-8 encoding.
     */
    private int getParamMaxLength() throws UnsupportedEncodingException {
        int length = 0;
        length += 16 * 2; // tagId + tagLength
        length += 28; // fixed sizes
        length += getPrimaryVersionCounter() == INVALID_VALUE_PARAMETER ? 0 : 16;
        length += getSecondaryVersionCounter() == INVALID_VALUE_PARAMETER ? 0 : 16;
        length += getDatabaseIdentifier() == INVALID_VALUE_PARAMETER ? 0 : 16;
        if (getSearchValue() != null)
            length += getSearchValue().getBytes("UTF-8").length;
        return length;
    }

    /**
     * Encode the application parameter object to a byte array.
     * 
     * @return a byte Array representation of the application parameter object.
     * @throws UnsupportedEncodingException
     *             if the platform does not support UTF-8 encoding.
     */
    public byte[] EncodeParams() throws UnsupportedEncodingException {
        ByteBuffer appParamBuf = ByteBuffer.allocate(getParamMaxLength());
        appParamBuf.order(ByteOrder.BIG_ENDIAN);
        byte[] retBuf;

        if (getOrder() != INVALID_VALUE_PARAMETER) {
            appParamBuf.put((byte) ORDER);
            appParamBuf.put((byte) BYTE_LEN_1);
            appParamBuf.putShort((short) getOrder());
        }
        if (getSearchValue() != null) {
            appParamBuf.put((byte) SEARCH_VALUE);
            appParamBuf.put((byte) START_OFFSET_LEN);
            appParamBuf.put((byte) getSearchValue().getBytes("UTF-8").length);
            appParamBuf.put(getSearchValue().getBytes("UTF-8"));
        }
        if (getSearchProperty() != INVALID_VALUE_PARAMETER) {
            appParamBuf.put((byte) SEARCH_PROPERTY);
            appParamBuf.put((byte) BYTE_LEN_1);
            appParamBuf.putShort((short) getSearchProperty());

        }
        if (getMaxListCount() != INVALID_VALUE_PARAMETER) {
            appParamBuf.put((byte) MAX_LIST_COUNT);
            appParamBuf.put((byte) BYTE_LEN_2);
            appParamBuf.putShort((short) getMaxListCount());
        }
        if (getListStartOffset() != INVALID_VALUE_PARAMETER) {
            appParamBuf.put((byte) LIST_START_OFFSET);
            appParamBuf.put((byte) BYTE_LEN_2);
            appParamBuf.putShort((short) getListStartOffset());
        }
        if (getPropertySelector() != INVALID_VALUE_PARAMETER) {
            appParamBuf.put((byte) PROPERTY_SELECTOR);
            appParamBuf.put((byte) BYTE_LEN_8);
            appParamBuf.putLong((long) getPropertySelector());
        }
        if (getFormat() != INVALID_VALUE_PARAMETER) {
            appParamBuf.put((byte) FORMAT);
            appParamBuf.put((byte) BYTE_LEN_1);
            appParamBuf.putShort((short) getFormat());
        }
        if (getPhonebookSize() != INVALID_VALUE_PARAMETER) {
            appParamBuf.put((byte) PHONEBOOK_SIZE);
            appParamBuf.put((byte) BYTE_LEN_2);
            appParamBuf.putShort((short) getPhonebookSize());
        }
        if (getNewMissedCalls() != INVALID_VALUE_PARAMETER) {
            appParamBuf.put((byte) NEW_MISSED_CALLS);
            appParamBuf.put((byte) BYTE_LEN_1);
            appParamBuf.putShort((short) getNewMissedCalls());
        }
        if (getPrimaryVersionCounter() != INVALID_VALUE_PARAMETER) {
            appParamBuf.put((byte) PRIMARY_VERSION_COUNTER);
            appParamBuf.put((byte) BYTE_LEN_16);
            appParamBuf.putDouble((double) getPrimaryVersionCounter());
        }
        if (getSecondaryVersionCounter() != INVALID_VALUE_PARAMETER) {
            appParamBuf.put((byte) SECONDARY_VERSION_COUNTRY);
            appParamBuf.put((byte) BYTE_LEN_16);
            appParamBuf.putDouble((double) getSecondaryVersionCounter());
        }
        if (getvCardSelector() != INVALID_VALUE_PARAMETER) {
            appParamBuf.put((byte) VCARD_SELECTOR);
            appParamBuf.put((byte) BYTE_LEN_8);
            appParamBuf.putLong((long) getvCardSelector());
        }
        if (getDatabaseIdentifier() != INVALID_VALUE_PARAMETER) {
            appParamBuf.put((byte) DATABASE_IDENTIFIER);
            appParamBuf.put((byte) BYTE_LEN_16);
            appParamBuf.putDouble((double) getDatabaseIdentifier());
        }
        if (getvCardSelectorOperator() != INVALID_VALUE_PARAMETER) {
            appParamBuf.put((byte) VCARD_SELECTOR_OPERATOR);
            appParamBuf.put((byte) BYTE_LEN_1);
            appParamBuf.putShort((short) getvCardSelectorOperator());
        }
        if (getResetNewMissedCalls() != INVALID_VALUE_PARAMETER) {
            appParamBuf.put((byte) RESET_NEW_MISSED_CALLS);
            appParamBuf.put((byte) BYTE_LEN_1);
            appParamBuf.putShort((short) getResetNewMissedCalls());
        }
        if (getPbapSupportedFeatures() != INVALID_VALUE_PARAMETER) {
            appParamBuf.put((byte) PBAP_SUPPORTED_FEATURES);
            appParamBuf.put((byte) BYTE_LEN_4);
            appParamBuf.putShort((short) getPbapSupportedFeatures());
        }

        // We need to reduce the length of the array to match the content
        retBuf = Arrays.copyOfRange(appParamBuf.array(), appParamBuf.arrayOffset(),
                appParamBuf.arrayOffset() + appParamBuf.position());
        return retBuf;
    }
}
