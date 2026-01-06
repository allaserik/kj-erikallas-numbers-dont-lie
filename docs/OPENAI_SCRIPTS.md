## Shell scripts to toggle API keys on/off (reliably)

The most common reason “my script doesn’t turn them on” is: you run it like `./set-openai.sh`, which executes in a  **subshell** , so exports disappear after the script ends.

You must **source** it so it modifies your current shell environment.

### Create `scripts/openai-on.sh`

<pre class="overflow-visible! px-0!" data-start="2103" data-end="2384"><div class="contain-inline-size rounded-2xl corner-superellipse/1.1 relative bg-token-sidebar-surface-primary"><div class="sticky top-[calc(--spacing(9)+var(--header-height))] @w-xl/main:top-9"><div class="absolute end-0 bottom-0 flex h-9 items-center pe-2"><div class="bg-token-bg-elevated-secondary text-token-text-secondary flex items-center gap-4 rounded-sm px-2 font-sans text-xs"></div></div></div><div class="overflow-y-auto p-4" dir="ltr"><code class="whitespace-pre! language-bash"><span><span>#!/usr/bin/env bash</span><span>
</span><span>set</span><span> -euo pipefail

</span><span>export</span><span> OPENAI_API_KEY=</span><span>"sk-proj-REPLACE_ME"</span><span>
</span><span>export</span><span> OPENAI_MODEL=</span><span>"gpt-4o-mini"</span><span>

</span><span>echo</span><span></span><span>"OpenAI environment variables set."</span><span>
</span><span>echo</span><span></span><span>"OPENAI_API_KEY is set to ${OPENAI_API_KEY:0:8}</span><span>... (hidden)"
</span><span>echo</span><span></span><span>"OPENAI_MODEL is set to ${OPENAI_MODEL}</span><span>"
</span></span></code></div></div></pre>

### Create `scripts/openai-off.sh`

<pre class="overflow-visible! px-0!" data-start="2421" data-end="2555"><div class="contain-inline-size rounded-2xl corner-superellipse/1.1 relative bg-token-sidebar-surface-primary"><div class="sticky top-[calc(--spacing(9)+var(--header-height))] @w-xl/main:top-9"><div class="absolute end-0 bottom-0 flex h-9 items-center pe-2"><div class="bg-token-bg-elevated-secondary text-token-text-secondary flex items-center gap-4 rounded-sm px-2 font-sans text-xs"></div></div></div><div class="overflow-y-auto p-4" dir="ltr"><code class="whitespace-pre! language-bash"><span><span>#!/usr/bin/env bash</span><span>
</span><span>set</span><span> -euo pipefail

</span><span>unset</span><span> OPENAI_API_KEY
</span><span>unset</span><span> OPENAI_MODEL

</span><span>echo</span><span></span><span>"OpenAI environment variables unset."</span><span>
</span></span></code></div></div></pre>

### Make executable

<pre class="overflow-visible! px-0!" data-start="2577" data-end="2640"><div class="contain-inline-size rounded-2xl corner-superellipse/1.1 relative bg-token-sidebar-surface-primary"><div class="sticky top-[calc(--spacing(9)+var(--header-height))] @w-xl/main:top-9"><div class="absolute end-0 bottom-0 flex h-9 items-center pe-2"><div class="bg-token-bg-elevated-secondary text-token-text-secondary flex items-center gap-4 rounded-sm px-2 font-sans text-xs"></div></div></div><div class="overflow-y-auto p-4" dir="ltr"><code class="whitespace-pre! language-bash"><span><span>chmod</span><span> +x scripts/openai-on.sh scripts/openai-off.sh
</span></span></code></div></div></pre>

### IMPORTANT: run with `source`

<pre class="overflow-visible! px-0!" data-start="2675" data-end="2742"><div class="contain-inline-size rounded-2xl corner-superellipse/1.1 relative bg-token-sidebar-surface-primary"><div class="sticky top-[calc(--spacing(9)+var(--header-height))] @w-xl/main:top-9"><div class="absolute end-0 bottom-0 flex h-9 items-center pe-2"><div class="bg-token-bg-elevated-secondary text-token-text-secondary flex items-center gap-4 rounded-sm px-2 font-sans text-xs"></div></div></div><div class="overflow-y-auto p-4" dir="ltr"><code class="whitespace-pre! language-bash"><span><span>source</span><span> scripts/openai-on.sh
</span><span># or</span><span>
. scripts/openai-on.sh
</span></span></code></div></div></pre>

To turn off:

<pre class="overflow-visible! px-0!" data-start="2757" data-end="2797"><div class="contain-inline-size rounded-2xl corner-superellipse/1.1 relative bg-token-sidebar-surface-primary"><div class="sticky top-[calc(--spacing(9)+var(--header-height))] @w-xl/main:top-9"><div class="absolute end-0 bottom-0 flex h-9 items-center pe-2"><div class="bg-token-bg-elevated-secondary text-token-text-secondary flex items-center gap-4 rounded-sm px-2 font-sans text-xs"></div></div></div><div class="overflow-y-auto p-4" dir="ltr"><code class="whitespace-pre! language-bash"><span><span>source</span><span> scripts/openai-off.sh
</span></span></code></div></div></pre>

### Verify they’re actually set in your current shell

<pre class="overflow-visible! px-0!" data-start="2853" data-end="2905"><div class="contain-inline-size rounded-2xl corner-superellipse/1.1 relative bg-token-sidebar-surface-primary"><div class="sticky top-[calc(--spacing(9)+var(--header-height))] @w-xl/main:top-9"><div class="absolute end-0 bottom-0 flex h-9 items-center pe-2"><div class="bg-token-bg-elevated-secondary text-token-text-secondary flex items-center gap-4 rounded-sm px-2 font-sans text-xs"></div></div></div><div class="overflow-y-auto p-4" dir="ltr"><code class="whitespace-pre! language-bash"><span><span>env</span><span> | grep -E </span><span>'^OPENAI_(API_KEY|MODEL)='</span><span>
</span></span></code></div></div></pre>

If that prints nothing after `openai-on.sh`, you didn’t source it.
