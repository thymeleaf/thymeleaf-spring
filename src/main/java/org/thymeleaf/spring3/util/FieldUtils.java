/*
 * =============================================================================
 * 
 *   Copyright (c) 2011-2012, The THYMELEAF team (http://www.thymeleaf.org)
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 * =============================================================================
 */
package org.thymeleaf.spring3.util;

import java.util.Arrays;
import java.util.List;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.BindStatus;
import org.springframework.web.servlet.support.RequestContext;
import org.thymeleaf.Arguments;
import org.thymeleaf.Configuration;
import org.thymeleaf.context.IProcessingContext;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.spring3.naming.SpringContextVariableNames;
import org.thymeleaf.standard.expression.Expression;
import org.thymeleaf.standard.expression.SelectionVariableExpression;
import org.thymeleaf.standard.expression.StandardExpressionProcessor;
import org.thymeleaf.standard.expression.VariableExpression;



/**
 * 
 * @author Daniel Fern&aacute;ndez
 * 
 * @since 1.0
 *
 */
public class FieldUtils {

    public static final String ALL_FIELDS = "*";
    public static final String ALL_FIELDS_FIELD_EXPRESSION = "*{*}";
    

    
    

    

    public static boolean hasErrors(final Arguments arguments, final String field) {
        return hasErrors(arguments.getConfiguration(), arguments, field);
    }
    
    public static boolean hasErrors(final Configuration configuration, 
            final IProcessingContext processingContext, final String field) {
        final BindStatus bindStatus = 
            FieldUtils.getBindStatus(configuration, processingContext, convertToFieldExpression(field), true);
        return bindStatus.isError();
        
    }
    
    

    public static List<String> errors(final Arguments arguments, final String field) {
        return errors(arguments.getConfiguration(), arguments, field);
    }

    public static List<String> errors(final Configuration configuration,
            final IProcessingContext processingContext, final String field) {

        final BindStatus bindStatus = 
            FieldUtils.getBindStatus(configuration, processingContext, convertToFieldExpression(field), true);
        
        final String[] errorCodes = bindStatus.getErrorMessages();
        return Arrays.asList(errorCodes);
        
    }
    
    public static List<String> errorsGlobal(final Configuration configuration,
            final IProcessingContext processingContext) {

        final BindStatus bindStatus = 
            FieldUtils.getBindStatusGlobal(configuration, processingContext);
        
        final String[] errorCodes = bindStatus.getErrorMessages();
        return Arrays.asList(errorCodes);
        
    }


    public static String idFromName(final String fieldName) {
        return StringUtils.deleteAny(fieldName, "[]");
    }
    
    
    
    private static String convertToFieldExpression(final String field) {
        return "*{" + field + "}";
    }
    
    

    
    

    public static BindStatus getBindStatus(
            final Arguments arguments, final String fieldExpression, final boolean allowAllFields) {
        return getBindStatus(arguments.getConfiguration(), arguments, fieldExpression, allowAllFields);
    }
    
    public static BindStatus getBindStatusGlobal(
            final Arguments arguments) {
        return getBindStatusGlobal(arguments.getConfiguration(), arguments);
    }
    
    
    public static BindStatus getBindStatus(final Configuration configuration, 
            final IProcessingContext processingContext, final String fieldExpression, final boolean allowAllFields) {
        
        final RequestContext requestContext =
            (RequestContext) processingContext.getContext().getVariables().get(SpringContextVariableNames.SPRING_REQUEST_CONTEXT);
        if (requestContext == null) {
            throw new TemplateProcessingException("A request context has not been created");
        }

        if (allowAllFields && ALL_FIELDS_FIELD_EXPRESSION.equals(fieldExpression)) {
            
            final String completeExpression = 
                FieldUtils.validateAndGetValueExpressionForAllFields(processingContext, false);
            
            return new BindStatus(requestContext, completeExpression, false);
            
        }
        
        final Expression expression = 
            StandardExpressionProcessor.parseExpression(configuration, processingContext, fieldExpression);
        
        final String completeExpression = 
            FieldUtils.validateAndGetValueExpressionForField(processingContext, expression);
        
        return new BindStatus(requestContext, completeExpression, false);
        
    }
    
    public static BindStatus getBindStatusGlobal(final Configuration configuration, 
            final IProcessingContext processingContext) {
        
        final RequestContext requestContext =
            (RequestContext) processingContext.getContext().getVariables().get(SpringContextVariableNames.SPRING_REQUEST_CONTEXT);
        if (requestContext == null) {
            throw new TemplateProcessingException("A request context has not been created");
        }

        final String completeExpression = 
                FieldUtils.validateAndGetValueExpressionForAllFields(processingContext, true);
        return new BindStatus(requestContext, completeExpression, false);
        
        
    }

    
    
    
    private static String validateAndGetValueExpressionForField(
            final IProcessingContext processingContext, final Expression expression) {

        /*
         * Only asterisk syntax (selection variable expressions) are allowed here.
         */
        
        if (expression instanceof SelectionVariableExpression) {

            final VariableExpression formCommandValue = 
                (VariableExpression) processingContext.getLocalVariable(SpringContextVariableNames.SPRING_FORM_COMMAND_VALUE);
            if (formCommandValue == null) {
                throw new TemplateProcessingException(
                        "Cannot process field expression " + expression + " as no form model object has " +
                        "been established in the \"form\" tag");
            }
            
            final String formCommandExpression = formCommandValue.getExpression();
            return formCommandExpression + "." + ((SelectionVariableExpression)expression).getExpression();
            
        }
        
        throw new TemplateProcessingException(
                "Expression \"" + expression + "\" is not valid: only selection variable expressions " +
                "*{...} are allowed in field specifications");
        
    }
    
    
    
    private static String validateAndGetValueExpressionForAllFields(
            final IProcessingContext processingContext, boolean onlyGlobal) {

        final VariableExpression formCommandValue = 
            (VariableExpression) processingContext.getLocalVariable(SpringContextVariableNames.SPRING_FORM_COMMAND_VALUE);
        if (formCommandValue == null) {
            throw new TemplateProcessingException(
                    "Cannot process expression for all fields \"" + ALL_FIELDS + "\" as no form model object has " +
                    "been established in the \"form\" tag");
        }
        final String formCommandExpression = formCommandValue.getExpression();
        if(onlyGlobal) {
            return formCommandExpression;
        }
        return formCommandExpression + "." + ALL_FIELDS;
        
    }

    private FieldUtils() {
	super();
    }

	
}
