<template>
  <footer class="site-footer" :class="{ compact }">
    <div class="footer-grid" aria-hidden="true"></div>
    <div class="footer-inner">
      <div class="footer-top">
        <p class="terminal-line">
          <span class="prompt">&gt;_</span>
          <span class="typing">HAN.SHUCHENG.init()</span>
          <span class="cursor-blink">█</span>
        </p>
        <p class="footer-tagline">极客出品 · 代码即信仰 · 帅到不像话</p>
      </div>

      <div class="footer-divider">
        <span class="divider-glow"></span>
      </div>

      <div class="footer-bottom">
        <p class="copyright">
          © {{ year }}
          <strong class="author">韩淑成</strong>
          <span class="rights">· All Rights Reserved</span>
        </p>
        <p class="footer-meta">
          <span>Shopmate 店小二</span>
          <span class="sep">|</span>
          <span>Vue 3</span>
          <span class="sep">|</span>
          <span>Spring AI</span>
        </p>
        <p v-if="!compact" class="footer-quote">
          // 写得了代码，聊得了天，改得了差评 —— 这才是真·极客
        </p>
      </div>
    </div>
  </footer>
</template>

<script setup>
defineProps({
  compact: { type: Boolean, default: false },
})

const year = new Date().getFullYear()
</script>

<style scoped>
.site-footer {
  position: relative;
  background: linear-gradient(180deg, #080a0f 0%, #050608 100%);
  border-top: 1px solid rgba(0, 212, 255, 0.12);
  color: var(--color-text-muted);
  overflow: hidden;
  flex-shrink: 0;
}

.site-footer.compact .footer-inner {
  padding: 16px var(--safe-x);
}

.site-footer.compact .footer-tagline,
.site-footer.compact .footer-quote {
  display: none;
}

.footer-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(0, 212, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 212, 255, 0.03) 1px, transparent 1px);
  background-size: 24px 24px;
  mask-image: linear-gradient(180deg, transparent, #000 30%, #000);
  pointer-events: none;
}

.footer-inner {
  position: relative;
  max-width: 1100px;
  margin: 0 auto;
  padding: 28px var(--safe-x) 24px;
  text-align: center;
}

.terminal-line {
  font-family: var(--font-mono);
  font-size: clamp(13px, 2.5vw, 15px);
  color: var(--color-accent);
  margin-bottom: 8px;
}

.prompt {
  color: var(--color-success);
  margin-right: 6px;
}

.typing {
  letter-spacing: 0.05em;
}

.cursor-blink {
  animation: blink 1s step-end infinite;
  color: var(--color-accent-warm);
}

@keyframes blink {
  50% {
    opacity: 0;
  }
}

.footer-tagline {
  font-size: clamp(14px, 3vw, 18px);
  font-weight: 700;
  background: linear-gradient(90deg, #00d4ff, #a78bfa, #f472b6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 0.08em;
}

.footer-divider {
  margin: 20px auto;
  max-width: 320px;
  height: 1px;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(0, 212, 255, 0.6),
    rgba(244, 114, 182, 0.6),
    transparent
  );
  position: relative;
}

.divider-glow {
  position: absolute;
  inset: -2px 20%;
  background: radial-gradient(ellipse, rgba(0, 212, 255, 0.4), transparent 70%);
  filter: blur(4px);
}

.copyright {
  font-family: var(--font-mono);
  font-size: 13px;
  color: var(--color-text);
  margin-bottom: 8px;
}

.author {
  color: var(--color-accent);
  font-weight: 700;
  text-shadow: 0 0 12px rgba(0, 212, 255, 0.5);
}

.rights {
  color: var(--color-text-dim);
  font-weight: 400;
}

.footer-meta {
  font-size: 12px;
  color: var(--color-text-dim);
  margin-bottom: 10px;
}

.sep {
  margin: 0 8px;
  opacity: 0.4;
}

.footer-quote {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--color-text-dim);
  opacity: 0.85;
}

@media (max-width: 480px) {
  .footer-meta .sep:nth-of-type(2) {
    display: none;
  }

  .footer-meta span:nth-child(4) {
    display: block;
    margin-top: 4px;
  }
}
</style>
