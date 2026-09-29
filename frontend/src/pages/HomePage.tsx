import { useEffect, useMemo, useState } from 'react'
import {
  ArrowRight, FileText, LifeBuoy, MessageSquare,
  Package, Plug, ShieldCheck, Sparkles, Zap,
} from 'lucide-react'
import { Link as RouterLink } from 'react-router-dom'
import { productsApi, type BillingPeriod, type Product } from '../api/productsApi'
import { useAuthModal } from '../auth/AuthModalContext'
import { SiteNavbar } from '../components/layout/SiteNavbar'
import { accentFor, iconFor } from '../utils/accentColor'
import '../styles/landing.css'

const HERO_IMAGE = 'https://taxfino.com/wp-content/uploads/2021/05/scr2.jpg'

function initials(name: string): string {
  const words = name.trim().split(/\s+/)
  return words.length >= 2
    ? (words[0][0] + words[1][0]).toUpperCase()
    : name.slice(0, 2).toUpperCase()
}

const BILLING_SUFFIX: Record<BillingPeriod, string> = { MONTHLY: '/month', YEARLY: '/year', ONE_TIME: '' }

function planFor(product: Product, period: BillingPeriod) {
  if (product.plans.length === 0) return null
  return product.plans.find((p) => p.billingPeriod === period) ?? product.plans[0]
}

/**
 * The "/" route — public marketing landing page. Ported from a reference
 * design (see CLAUDE.md decision 14) with its exact CSS (styles/landing.css,
 * scoped under .vyoog-landing) and DOM structure kept verbatim — every
 * section that describes actual products/pricing pulls real data from the
 * backend instead of the reference's hardcoded placeholder content, so this
 * page can never assert something about the catalog that isn't true.
 */
export function HomePage() {
  const authModal = useAuthModal()
  const [products, setProducts] = useState<Product[]>([])
  const [yearly, setYearly] = useState(false)

  useEffect(() => {
    productsApi.list().then(setProducts).catch(() => setProducts([]))
  }, [])

  const hasYearlyTier = useMemo(
    () => products.some((p) => p.plans.some((plan) => plan.billingPeriod === 'YEARLY')),
    [products],
  )

  const scrollTo = (id: string) => (e: React.MouseEvent) => {
    e.preventDefault()
    document.querySelector(id)?.scrollIntoView({ behavior: 'smooth' })
  }

  const categories = Array.from(new Set(products.map((p) => p.category?.trim()).filter((c): c is string => Boolean(c))))

  return (
    <div className="vyoog-landing">
      <SiteNavbar />

      <main id="main-content">
        <section className="hero">
          <div className="container hero-grid">
            <div className="hero-copy">
              <div className="eyebrow"><i><Sparkles size={11} /></i> Intelligent Business Platform</div>
              <h1>Smarter Business<br />Starts with <span className="gradient-word">Vyoog</span></h1>
              <p>
                EIS Platform is Vyoog's home for discovering, purchasing, and launching
                your products — with a single sign-on shared across every one of them.
              </p>
              <div className="hero-buttons">
                <button className="white-btn" onClick={scrollTo('#products')}>Explore Products →</button>
                <span className="hero-stat">
                  Vyoog Platform <span><strong>{String(products.length).padStart(2, '0')} Products</strong></span>
                  <em>●</em>
                </span>
              </div>
            </div>

            <div className="hero-visual">
              <div className="face-glow" />
              <div className="person" style={{ backgroundImage: `linear-gradient(90deg, rgba(3,5,15,.25), rgba(4,4,8,0) 42%), url(${HERO_IMAGE})` }} />
              <div className="ai-card">
                <h3>One login. Every product.</h3>
                <p>Sign in once and every Vyoog product you've configured is already connected.</p>
                <button onClick={scrollTo('#solutions')}>See how</button>
              </div>
              <div className="glow-orb" />
            </div>
          </div>

          {products.length > 0 && (
            <div className="container hero-bento">
              <div className="bento-tile bento-preview">
                <div className="bento-window">
                  <div className="bento-window-bar"><span /><span /><span /></div>
                  <div className="bento-window-body">
                    {products.slice(0, 3).map((product) => {
                      const accent = accentFor(product.name)
                      const Icon = iconFor(product.name)
                      const plan = product.plans.find((pl) => pl.billingPeriod === 'MONTHLY') ?? product.plans[0]
                      const price = plan ? plan.price : product.price
                      return (
                        <div className="bento-row" key={product.id}>
                          <span className="bento-row-icon" style={{ background: accent.bg, color: accent.fg }}>
                            <Icon size={14} />
                          </span>
                          <span className="bento-row-name">{product.name}</span>
                          <span className="bento-row-cat">{product.category ?? 'App'}</span>
                          <span className="bento-row-price">${price.toFixed(0)}/mo</span>
                        </div>
                      )
                    })}
                  </div>
                </div>
                <p className="bento-caption">Your product catalog, ready to launch.</p>
              </div>

              <div className="bento-tile bento-stat">
                <span className="bento-stat-num">{products.length}</span>
                <span className="bento-stat-label">product{products.length === 1 ? '' : 's'} live in the catalog</span>
              </div>

              {categories.length > 0 && (
                <div className="bento-tile bento-categories">
                  <span className="bento-tile-label">Categories</span>
                  <div className="bento-pill-row">
                    {categories.map((category) => (
                      <span className="bento-pill" key={category}>{category}</span>
                    ))}
                  </div>
                </div>
              )}

              <RouterLink to="/products" className="bento-tile bento-cta">
                <div>
                  <strong>Every product, one workspace.</strong>
                  <p>Browse the full catalog and see what's already connected.</p>
                </div>
                <span className="bento-cta-arrow"><ArrowRight size={16} /></span>
              </RouterLink>
            </div>
          )}
        </section>

        <section className="section" id="products">
          <div className="container">
            <div className="section-head">
              <div className="section-label">Vyoog Products</div>
              <h2>One platform. Every part of your business.</h2>
              <p>Choose the products you need today and expand into a connected Vyoog ecosystem as your organization grows.</p>
            </div>
            {products.length === 0 ? (
              <p style={{ textAlign: 'center', color: '#727785', fontSize: 13 }}>No products yet — check back soon.</p>
            ) : (
              <div className="products">
                {products.map((product) => (
                  <article className="product" key={product.id}>
                    <div className="product-icon">{initials(product.name)}</div>
                    <h3>{product.name}</h3>
                    {product.category && <span className="product-category">{product.category}</span>}
                    <p>{product.description ?? 'A Vyoog product, ready to launch from your workspace.'}</p>
                    <div className="product-actions">
                      <RouterLink className="product-link" to="/products">Explore →</RouterLink>
                      <button className="product-subscribe-btn" onClick={authModal.openRegister}>Subscribe</button>
                    </div>
                  </article>
                ))}
              </div>
            )}
          </div>
        </section>

        <section className="section dark" id="solutions">
          <div className="container">
            <div className="section-head">
              <div className="section-label">Solutions</div>
              <h2>Designed for real business outcomes.</h2>
              <p style={{ color: '#888e9e' }}>
                One identity, one catalog — every Vyoog product you launch already knows who you are.
              </p>
            </div>
            <div className="dark-grid">
              <div className="dark-card" id="solutions-sso">
                <div className="section-label">Single Sign-On</div>
                <h3>Log in once, everywhere.</h3>
                <p>Every product configured here shares the same Keycloak-backed identity, so a session started on EIS Platform carries straight through the moment you launch.</p>
                <div className="tags">
                  <span className="tag">Keycloak</span><span className="tag">SSO</span>
                  <span className="tag">Session hand-off</span><span className="tag">Role-based access</span>
                </div>
              </div>
              <div className="dark-card" id="solutions-catalog">
                <div className="section-label">Product Catalog</div>
                <h3>Everything you own, in one place.</h3>
                <p>Discover, purchase, and launch every Vyoog product from a single admin-managed catalog.</p>
                <div className="tags">
                  <span className="tag">Catalog</span><span className="tag">Launch</span><span className="tag">Admin-managed</span>
                </div>
              </div>
            </div>
            <div className="industry-grid">
              <div className="industry"><b>Teams</b><p>One login for every tool your team already uses.</p></div>
              <div className="industry"><b>Admins</b><p>Manage the catalog and pricing from one dashboard.</p></div>
              <div className="industry"><b>New hires</b><p>Onboard once, get access to everything they need.</p></div>
              <div className="industry"><b>Growing orgs</b><p>Add a product to the catalog whenever you're ready.</p></div>
            </div>
          </div>
        </section>

        <section className="section pricing" id="pricing">
          <div className="container">
            <div className="section-head">
              <div className="section-label">Pricing</div>
              <h2>Choose the way you want to grow.</h2>
              <p>Real pricing from your product catalog — not a placeholder table.</p>
            </div>
            {hasYearlyTier && (
              <div className="billing">
                <button className={yearly ? '' : 'active'} onClick={() => setYearly(false)}>Monthly</button>
                <button className={yearly ? 'active' : ''} onClick={() => setYearly(true)}>Yearly — Save more</button>
              </div>
            )}
            {products.length === 0 ? (
              <p style={{ textAlign: 'center', color: '#727785', fontSize: 13 }}>No products yet — check back soon.</p>
            ) : (
              <div className="plans">
                {products.map((product, i) => {
                  const period: BillingPeriod = yearly ? 'YEARLY' : 'MONTHLY'
                  const plan = planFor(product, period)
                  const price = plan ? plan.price : product.price
                  const suffix = plan ? BILLING_SUFFIX[plan.billingPeriod] : ''
                  const featured = products.length > 1 && i === 0

                  return (
                    <div className={`plan${featured ? ' featured' : ''}`} key={product.id}>
                      {featured && <span className="badge">MOST POPULAR</span>}
                      <h3>{product.name}</h3>
                      <div className="price">${price.toFixed(0)}{suffix && <span>{suffix}</span>}</div>
                      <p>{product.description ?? 'A Vyoog product.'}</p>
                      <ul>
                        <li><ShieldCheck size={11} color="#6175ed" /> Single sign-on included</li>
                        <li><Package size={11} color="#6175ed" /> Launch from your workspace</li>
                        <li><Zap size={11} color="#6175ed" /> Cancel anytime</li>
                      </ul>
                      <button onClick={authModal.openRegister}>
                        {featured ? 'Start now' : 'Get started'}
                      </button>
                    </div>
                  )
                })}
              </div>
            )}
          </div>
        </section>

        <section className="section company" id="company">
          <div className="container">
            <div className="section-head">
              <div className="section-label">About Vyoog</div>
              <h2>Build a better digital business.</h2>
              <p>EIS Platform is Vyoog's home for making product discovery, purchase, and single sign-on simpler and more connected.</p>
            </div>
            <div className="company-grid">
              <div className="company-card dark-company">
                <div className="section-label">Our vision</div>
                <h3>Technology should simplify business, not add complexity.</h3>
                <p>One catalog, one identity — organizations manage today and add tomorrow's product without starting over.</p>
                <div className="numbers">
                  <div className="number"><strong>{String(products.length).padStart(2, '0')}</strong><span>Products</span></div>
                  <div className="number"><strong>01</strong><span>Ecosystem</span></div>
                  <div className="number"><strong>∞</strong><span>Possibilities</span></div>
                </div>
              </div>
              <div className="company-card">
                <div className="section-label">Why EIS Platform</div>
                <h3>Connected by design.</h3>
                <p>Use one product or build a complete software environment around your business.</p>
                <div className="value-grid">
                  <div className="value"><b>Connected</b><span>Products designed to work together.</span></div>
                  <div className="value"><b>Practical</b><span>Built around actual business workflows.</span></div>
                  <div className="value"><b>Scalable</b><span>Start small and expand as you grow.</span></div>
                  <div className="value"><b>Secure</b><span>Role-based and controlled business access.</span></div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section className="section dark" id="resources">
          <div className="container">
            <div className="section-head">
              <div className="section-label">Resources</div>
              <h2>Everything you need to succeed with Vyoog.</h2>
              <p style={{ color: '#888e9e' }}>Documentation, support, and the integrations behind every Vyoog product.</p>
            </div>
            <div className="products">
              <article className="product">
                <div className="product-icon"><FileText size={18} /></div>
                <h3>Documentation</h3>
                <p>Guides for setting up your products, team access, and roles.</p>
                <a className="product-link" href="#resources">Explore docs →</a>
              </article>
              <article className="product">
                <div className="product-icon"><MessageSquare size={18} /></div>
                <h3>Customer stories</h3>
                <p>See how organizations connect their teams around Vyoog products.</p>
                <a className="product-link" href="#resources">View stories →</a>
              </article>
              <article className="product">
                <div className="product-icon"><LifeBuoy size={18} /></div>
                <h3>Support</h3>
                <p>A simple path to technical assistance and service requests.</p>
                <a className="product-link" href="#contact">Get support →</a>
              </article>
              <article className="product">
                <div className="product-icon"><Plug size={18} /></div>
                <h3>Integrations</h3>
                <p>Single sign-on, Keycloak, and the APIs behind the catalog.</p>
                <a className="product-link" href="#resources">Explore →</a>
              </article>
            </div>
          </div>
        </section>

        <section className="cta" id="contact">
          <div className="container">
            <div className="cta-box">
              <div className="section-label">Start with Vyoog</div>
              <h2>Ready to make your<br />business smarter?</h2>
              <p>Register your organization, select your products and create your connected Vyoog workspace.</p>
              <button className="white-btn" onClick={authModal.openRegister}>Get started Now →</button>
            </div>
          </div>
        </section>
      </main>

      <footer className="footer">
        <div className="container">
          <div className="footer-grid">
            <div>
              <div className="brand">
                <img src="https://www.vyoog.com/wp-content/uploads/2022/03/evyoog-logonew1.png" alt="" className="brand-logo" />
                EIS Platform
              </div>
              <p style={{ fontSize: 10, lineHeight: 1.7, maxWidth: 270 }}>
                Connected business software for operations, planning, people and growth.
              </p>
            </div>
            <div>
              <h4>Products</h4>
              {products.length === 0
                ? <RouterLink to="/products">Browse products</RouterLink>
                : products.map((p) => <RouterLink to="/products" key={p.id}>{p.name}</RouterLink>)}
            </div>
            <div>
              <h4>Solutions</h4>
              <a href="#solutions" onClick={scrollTo('#solutions')}>Single Sign-On</a>
              <a href="#solutions" onClick={scrollTo('#solutions')}>Product Catalog</a>
            </div>
            <div>
              <h4>Company</h4>
              <a href="#company" onClick={scrollTo('#company')}>About Vyoog</a>
              <a href="#pricing" onClick={scrollTo('#pricing')}>Pricing</a>
              <a href="#resources" onClick={scrollTo('#resources')}>Resources</a>
            </div>
          </div>
          <div className="copy">© {new Date().getFullYear()} Vyoog Information Pvt Ltd. All rights reserved.</div>
        </div>
      </footer>
    </div>
  )
}
