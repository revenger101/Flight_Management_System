import { Component } from 'react';

export default class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error) {
    console.error('Application error boundary caught an error:', error);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="app-fallback">
          <h1>Something went wrong</h1>
          <p>The page crashed unexpectedly. Please refresh and try again.</p>
          <button className="btn btn-primary" onClick={() => window.location.reload()}>Reload App</button>
        </div>
      );
    }

    return this.props.children;
  }
}
