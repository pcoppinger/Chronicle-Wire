/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.wire.channel;

/**
 * Abstract class supporting the common fields implied by ChannelHandler
 *
 * @param <H> the same class so setters can return this
 */
public abstract class AbstractHandler<H extends AbstractHandler<H>>
        extends AbstractHeader<H>
        implements ChannelHandler {
    private Boolean buffered;

    public Boolean buffered() {
        return buffered;
    }

    /**
     * @param buffered determine if a channel should be buffered on the other side, or null if left to the server
     * @return this
     */
    public H buffered(Boolean buffered) {
        this.buffered = buffered;
        return (H) this;
    }
}
