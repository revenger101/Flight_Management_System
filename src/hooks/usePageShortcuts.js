import { useEffect } from 'react';

function isTypingElement(target) {
  const tagName = (target?.tagName || '').toLowerCase();
  return tagName === 'input' || tagName === 'textarea' || target?.isContentEditable;
}

export function usePageShortcuts({ onCreate, onSearch, onCloseModal, modalOpen }) {
  useEffect(() => {
    const onKeyDown = (e) => {
      if (e.key === 'Escape' && modalOpen) {
        e.preventDefault();
        onCloseModal?.();
        return;
      }

      if (isTypingElement(e.target)) return;

      if (e.key.toLowerCase() === 'n') {
        e.preventDefault();
        onCreate?.();
      }

      if (e.key === '/') {
        e.preventDefault();
        onSearch?.();
      }
    };

    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [modalOpen, onCloseModal, onCreate, onSearch]);
}
