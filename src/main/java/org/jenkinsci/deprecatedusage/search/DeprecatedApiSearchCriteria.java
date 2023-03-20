/*
 * The MIT License
 *
 * Copyright (c) 2021, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.deprecatedusage.search;

import org.jenkinsci.deprecatedusage.DeprecatedApi;

public class DeprecatedApiSearchCriteria implements SearchCriteria {
    private DeprecatedApi deprecatedApi;

    public DeprecatedApiSearchCriteria(DeprecatedApi deprecatedApi) {
        this.deprecatedApi = deprecatedApi;
    }

    @Override 
    public boolean isLookingForClass(String className) {
        return deprecatedApi.getClasses().contains(className);
    }

    @Override 
    public boolean isLookingForMethod(String methodKey, String className, String methodName) {
        return deprecatedApi.getMethods().contains(methodKey) || methodName.equals("getDescriptor") || methodName.startsWith("do");
    }

    @Override 
    public boolean isLookingForField(String fieldKey, String className, String fieldName) {
        return deprecatedApi.getFields().contains(fieldKey);
    }

    @Override
    public boolean shouldAnalyzeClass(String className) {
        return true;
    }
}
