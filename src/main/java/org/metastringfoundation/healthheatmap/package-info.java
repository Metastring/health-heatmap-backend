/**
 * This is the backend for health heatmap.
 * <p>
 * It is written in a hexagonal architecture with clear separation of domains into:
 * <ul>
 *     <li>{@link org.metastringfoundation.healthheatmap.cli} - Any interaction through CLI</li>
 *     <li>{@link org.metastringfoundation.healthheatmap.logic} - Application logic</li>
 *     <li>{@link org.metastringfoundation.healthheatmap.storage} - Storage and database queries</li>
 *     <li>{@link org.metastringfoundation.healthheatmap.web} - REST APIs</li>
 * </ul>
 */
package org.metastringfoundation.healthheatmap;