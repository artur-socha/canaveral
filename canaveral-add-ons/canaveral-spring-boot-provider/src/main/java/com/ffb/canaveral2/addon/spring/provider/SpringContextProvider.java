/*
 * Software is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 *
 * The Initial Developer of the Original Code is Paweł Kamiński.
 * All Rights Reserved.
 */
package com.ffb.canaveral2.addon.spring.provider;

import org.springframework.context.ApplicationContext;

public interface SpringContextProvider
{
    ApplicationContext getSpringApplicationContext();
}
